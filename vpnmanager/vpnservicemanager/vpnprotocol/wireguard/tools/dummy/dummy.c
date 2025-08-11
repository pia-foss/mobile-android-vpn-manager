// tools/dummy/dummy.c
#include <stdio.h>
#include <string.h>

__attribute__((noinline))
static const char *get_src(void) {
    /* keep in static storage so it's not optimized into the caller */
    static const char s[] = "This string is longer than eight characters.";
    return s;
}

__attribute__((noinline))
static void fortified_function(void) {
    /* small destination buffer with compile-time size known to compiler */
    char buf[8];
    const char *s = get_src();
    /* strcpy() here should be replaced by __strcpy_chk when _FORTIFY_SOURCE=2 + -O2 */
    strcpy(buf, s);
}

/* run at load time to keep the code in the final DSO (prevents linker GC) */
__attribute__((constructor))
static void init_dummy(void) {
    fortified_function();
    /* avoid optimizing-out the call chain completely */
    (void)printf("dummy.so initialized\n");
}
