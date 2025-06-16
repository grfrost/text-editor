#include <sys/ioctl.h>
#include <termios.h>
#include <stdio.h>

#define SHOW(N) { int N##i =  N; printf("%10s=0x%08x\n", "" #N "", N); }
#define GEN(N) { int N##i =  N; printf("    public static final bit %-10s = bit.of(\"%s\",0x%08x);\n", "" #N "", "" #N "", N); }

int main(int argc, char **arg){
    printf("sizeof(TIOCGWINSZ)=%ld bytes\n", sizeof(TIOCGWINSZ));
    printf("sizeof(long)=%ld bytes\n", sizeof(long));
    printf("sizeof(int)=%ld bytes\n", sizeof(int));
    printf("sizeof(termios_t)=%ld bytes\n", sizeof(struct termios));
    struct termios t;
    void *addr = &t;

    printf("sizeof(termios_t.c_iflag)=%5ld bytes @  %5ld \n", sizeof(t.c_iflag), (long)(((void*)&t.c_iflag) - addr));
    printf("sizeof(termios_t.c_oflag)=%5ld bytes @  %5ld \n", sizeof(t.c_oflag), (long)(((void*)&t.c_oflag) - addr));
    printf("sizeof(termios_t.c_cflag)=%5ld bytes @  %5ld \n", sizeof(t.c_cflag), (long)(((void*)&t.c_cflag) - addr));
    printf("sizeof(termios_t.c_lflag)=%5ld bytes @  %5ld \n", sizeof(t.c_lflag), (long)(((void*)&t.c_lflag) - addr));
    printf("sizeof(termios_t.c_cc   )=%5ld bytes @  %5ld \n", sizeof(t.c_cc),    (long)(((void*)&t.c_cc) - addr));

    SHOW(NCCS);
    SHOW(TIOCGWINSZ);

    printf("public record LFLAG(){\n");
    GEN(ECHO);
    GEN(ECHOE);
    GEN(ECHOK);
    GEN(ECHONL);
    GEN(ICANON);
    GEN(IEXTEN);
    GEN(ISIG);
    GEN(NOFLSH);
    GEN(TOSTOP);
    printf("}\n\n");

    printf("public record IFLAG(){\n");
    GEN(BRKINT);
    GEN(ICRNL);
    GEN(INLCR);
    GEN(IGNBRK);
    GEN(IXON);
    GEN(IXOFF);
    GEN(ISTRIP);
    printf("}\n\n");

    printf("public record OFLAG(){\n");
    GEN(OCRNL);
    GEN(ONLCR);
    GEN(OPOST);
    GEN(OLCUC);
    printf("}\n\n");

    struct winsize wbuf;


    if (ioctl(0,TIOCGWINSZ, &wbuf) ==0){
        printf("ws_col=%d,", wbuf.ws_col);
        printf("ws_row=%d,", wbuf.ws_row);
        printf("ws_xpixel=%d,", wbuf.ws_xpixel);
        printf("ws_ypixel=%d\n", wbuf.ws_ypixel);
    }else{
       printf("ioctl failed\n");
    }
}
