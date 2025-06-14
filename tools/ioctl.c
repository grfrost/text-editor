#include <sys/ioctl.h>
#include <termios.h>
#include <stdio.h>

int main(int argc, char **arg){
    long TIOCGWINSZl =TIOCGWINSZ ;
    int TIOCGWINSZi =TIOCGWINSZ ;
    int NCCSi =NCCS;
    printf("sizeof(TIOCGWINSZ)=%ld bytes\n", sizeof(TIOCGWINSZ));
    printf("sizeof(long)=%ld bytes\n", sizeof(long));
    printf("sizeof(int)=%ld bytes\n", sizeof(int));
    printf("sizeof(termios_t)=%ld bytes\n", sizeof(struct termios));
    printf("NCCS=%d bytes\n", NCCSi);
    printf("TIOCGWINSZl=0x%lx\n", TIOCGWINSZl);
    printf("TIOCGWINSZi=0x%x\n", TIOCGWINSZi);

    struct winsize wbuf;


    if (ioctl(0,TIOCGWINSZi, &wbuf) ==0){
        printf("ws_col=%d,", wbuf.ws_col);
        printf("ws_row=%d,", wbuf.ws_row);
        printf("ws_xpixel=%d,", wbuf.ws_xpixel);
        printf("ws_ypixel=%d\n", wbuf.ws_ypixel);
    }else{
       printf("ioctl failed\n");
    }
}
