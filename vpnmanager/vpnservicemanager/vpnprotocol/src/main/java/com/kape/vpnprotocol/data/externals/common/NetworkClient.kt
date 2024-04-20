/*
 *
 *  *  Copyright (c) "2023" Private Internet Access, Inc.
 *  *
 *  *  This file is part of the Private Internet Access Android Client.
 *  *
 *  *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  *  modify it under the terms of the GNU General Public License as published by the Free
 *  *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  *
 *  *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  *  details.
 *  *
 *  *  You should have received a copy of the GNU General Public License along with the Private
 *  *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package com.kape.vpnprotocol.data.externals.common

import com.kape.vpnprotocol.presenters.VPNProtocolError
import com.kape.vpnprotocol.presenters.VPNProtocolErrorCode
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.headers
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.encodeURLParameter
import io.ktor.util.StringValuesBuilderImpl
import io.ktor.util.appendAll
import okhttp3.OkHttpClient
import org.spongycastle.asn1.x500.X500Name
import org.spongycastle.asn1.x500.style.BCStyle
import java.io.ByteArrayOutputStream
import java.lang.Error
import java.security.InvalidKeyException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException
import java.security.SecureRandom
import java.security.SignatureException
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Arrays
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLPeerUnverifiedException
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import javax.security.auth.x500.X500Principal

/*
 *  Copyright (c) 2022 Private Internet Access, Inc.
 *
 *  This file is part of the Private Internet Access Android Client.
 *
 *  The Private Internet Access Android Client is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  The Private Internet Access Android Client is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU General Public License along with the Private
 *  Internet Access Android Client.  If not, see <https://www.gnu.org/licenses/>.
 */

internal class NetworkClient : INetworkClient {

    companion object {
        private const val REQUEST_TIMEOUT_MS = 3000L
    }

    // region INetworkClient
    override suspend fun performGetRequest(
        host: String,
        port: Int,
        path: String,
        headers: List<Pair<String, String>>,
        parameters: List<Pair<String, String>>,
        certificate: String?,
        commonName: String,
    ): Result<String> {
        val parametersBuilder = ParametersBuilder(parameters.size)
        parameters.forEach { parametersBuilder.append(it.first, it.second.encodeURLParameter()) }
        val headersBuilder = StringValuesBuilderImpl(size = headers.size)
        headers.forEach { headersBuilder.append(it.first, it.second) }
        val networkClient = getNetworkClient(
            certificate = certificate,
            ipOrRootDomain = host,
            commonName = commonName
        )
        return try {
            val response: HttpResponse = networkClient.request {
                method = HttpMethod.Get
                url {
                    it.protocol = URLProtocol.HTTPS
                    it.host = host
                    it.port = port
                    it.pathSegments = listOf(path)
                    it.encodedParameters = parametersBuilder
                }
                headers {
                    appendAll(headersBuilder)
                }
            }
            Result.success(response.bodyAsText())
        } catch (exception: Exception) {
            Result.failure(
                VPNProtocolError(
                    code = VPNProtocolErrorCode.NETWORK_REQUEST_ERROR,
                    error = Error(exception.message, exception.cause)
                )
            )
        }
    }
    // endregion

    // region private
    private fun getNetworkClient(
        certificate: String?,
        ipOrRootDomain: String,
        commonName: String,
    ): HttpClient =
        HttpClient(OkHttp) {
            expectSuccess = true
            engine {
                preconfigured = getPreConfiguredNetworkClient(
                    certificate = certificate,
                    ipOrRootDomain = ipOrRootDomain,
                    commonName = commonName
                )
            }
        }

    private fun getPreConfiguredNetworkClient(
        certificate: String?,
        ipOrRootDomain: String,
        commonName: String,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
        certificate?.let {
            val keyStore = KeyStore.getInstance("BKS")
            keyStore.load(null)
            val inputStream = certificate.toByteArray().inputStream()
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val certificateObject = certificateFactory.generateCertificate(inputStream)
            keyStore.setCertificateEntry("vpnmanager", certificateObject)
            inputStream.close()
            val trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            trustManagerFactory.init(keyStore)
            val trustManagers = trustManagerFactory.trustManagers
            check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager)) {
                "Unexpected default trust managers:" + Arrays.toString(trustManagers)
            }
            val trustManager = trustManagers[0] as X509TrustManager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustManagers, SecureRandom())
            val sslSocketFactory = sslContext.socketFactory
            if (sslSocketFactory != null) {
                builder.sslSocketFactory(sslSocketFactory, trustManager)
            }
            builder.hostnameVerifier(AccountHostnameVerifier(trustManager, ipOrRootDomain, commonName))
        }

        builder.connectTimeout(REQUEST_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        return builder.build()
    }

    private class AccountHostnameVerifier(
        private val trustManager: X509TrustManager?,
        private val requestHostname: String,
        private val commonName: String,
    ) : HostnameVerifier {

        override fun verify(hostname: String?, session: SSLSession?): Boolean {
            var verified = false
            try {
                val x509CertificateChain = session?.peerCertificates as Array<out X509Certificate>
                trustManager?.checkServerTrusted(x509CertificateChain, "RSA")
                val sessionCertificate = session.peerCertificates.first()
                verified = verifyCommonName(hostname, sessionCertificate as X509Certificate)
            } catch (e: SSLPeerUnverifiedException) {
                e.printStackTrace()
            } catch (e: CertificateException) {
                e.printStackTrace()
            } catch (e: InvalidKeyException) {
                e.printStackTrace()
            } catch (e: NoSuchAlgorithmException) {
                e.printStackTrace()
            } catch (e: NoSuchProviderException) {
                e.printStackTrace()
            } catch (e: SignatureException) {
                e.printStackTrace()
            }
            return verified
        }

        private fun verifyCommonName(hostname: String?, certificate: X509Certificate): Boolean {
            var verified = false
            val principal = certificate.subjectDN as X500Principal
            certificateCommonName(X500Name.getInstance(principal.encoded))?.let { certCommonName ->
                verified = hostname?.let {
                    isEqual(it.toByteArray(), requestHostname.toByteArray()) &&
                        isEqual(commonName.toByteArray(), certCommonName.toByteArray())
                } ?: isEqual(commonName.toByteArray(), certCommonName.toByteArray())
            }
            return verified
        }

        private fun certificateCommonName(name: X500Name): String? {
            val rdns = name.getRDNs(BCStyle.CN)
            return if (rdns.isEmpty()) {
                null
            } else {
                rdns.first().first.value.toString()
            }
        }

        private fun isEqual(a: ByteArray, b: ByteArray): Boolean {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val random = SecureRandom()
            val randomBytes = ByteArray(20)
            random.nextBytes(randomBytes)

            val concatA = ByteArrayOutputStream()
            concatA.write(randomBytes)
            concatA.write(a)
            val digestA = messageDigest.digest(concatA.toByteArray())

            val concatB = ByteArrayOutputStream()
            concatB.write(randomBytes)
            concatB.write(b)
            val digestB = messageDigest.digest(concatB.toByteArray())

            return MessageDigest.isEqual(digestA, digestB)
        }
    }
    // endregion
}
