package themrcodes.composeui.term

import java.io.InputStream
import kotlin.experimental.and


class EmulatorVT100(
    val term: Term,
    val inputStream: InputStream
): Emulator(term, inputStream) {

    override fun start() {
        reset()

        val intarg = IntArray(10)
        var intargi: Int = 0

        x = 0
        y = char_height

        var b: Byte
        try {
            while (true) {
                b = char

                //System.out.println("@0: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
                //System.out.println("@0: ry="+ry);
                /*
                outputs from infocmp on RedHat8.0
        #       Reconstructed via infocmp from file: /usr/share/terminfo/v/vt100
        vt100|vt100-am|dec vt100 (w/advanced video),
                am, msgr, xenl, xon,
                cols#80, it#8, lines#24, vt#3,
        	acsc=``aaffggjjkkllmmnnooppqqrrssttuuvvwwxxyyzz{{||}}~~,
        	bel=^G, blink=\E[5m$<2>, bold=\E[1m$<2>,
        	clear=\E[H\E[J$<50>, cr=^M, csr=\E[%i%p1%d;%p2%dr,
        	cub=\E[%p1%dD, cub1=^H, cud=\E[%p1%dB, cud1=^J,
                cuf=\E[%p1%dC, cuf1=\E[C$<2>,
                cup=\E[%i%p1%d;%p2%dH$<5>, cuu=\E[%p1%dA,
                cuu1=\E[A$<2>, ed=\E[J$<50>, el=\E[K$<3>, el1=\E[1K$<3>,
                enacs=\E(B\E)0, home=\E[H, ht=^I, hts=\EH, ind=^J, ka1=\EOq,
                ka3=\EOs, kb2=\EOr, kbs=^H, kc1=\EOp, kc3=\EOn, kcub1=\EOD,
                kcud1=\EOB, kcuf1=\EOC, kcuu1=\EOA, kent=\EOM, kf0=\EOy,
                kf1=\EOP, kf10=\EOx, kf2=\EOQ, kf3=\EOR, kf4=\EOS, kf5=\EOt,
                kf6=\EOu, kf7=\EOv, kf8=\EOl, kf9=\EOw, rc=\E8,
                rev=\E[7m$<2>, ri=\EM$<5>, rmacs=^O, rmam=\E[?7l,
                rmkx=\E[?1l\E>, rmso=\E[m$<2>, rmul=\E[m$<2>,
                rs2=\E>\E[?3l\E[?4l\E[?5l\E[?7h\E[?8h, sc=\E7,
                sgr=\E[0%?%p1%p6%|%t;1%;%?%p2%t;4%;%?%p1%p3%|%t;7%;%?%p4%t;5%;m%?%p9%t\016%e\017%;$<2>,
                sgr0=\E[m\017$<2>, smacs=^N, smam=\E[?7h, smkx=\E[?1h\E=,
                smso=\E[7m$<2>, smul=\E[4m$<2>, tbc=\E[3g,
        */
                /*
                am    terminal has automatic margnins
                msgr  safe to move while in standout mode
                xenl  newline ignored after 80 cols (concept)
                xon   terminal uses xon/xoff handshake
                cols  number of columns in a line
                it    tabs initially every # spaces
                lines number of lines on screen of page
                vt    virstual terminal number(CB/unix)
                acsc  graphics charset pairs, based on vt100
                bel   bell
                blink turn on blinking
                bold  turn on bold(extra bright) mode
                clear clear screen and home cursor(P*)
                cr    carriage return (P)(P*)
                csr   change region to line #1 to line #2(P)
                cub   move #1 characters to the left (P)
                cub1  move left one space
                cud   down #1 lines (P*)
                cud1  down one line
                cuf   move to #1 characters to the right.
                cuf1  non-destructive space (move right one space)
                cup   move to row #1 columns #2
                cuu   up #1 lines (P*)
                cuu1  up one line
                ed    clear to end of screen (P*)
                el    clear to end of line (P)
                el1   Clear to begining of line
                enacs enable alterate char set
                home  home cursor (if no cup)
                ht    tab to next 8-space hardware tab stop
                hts   set a tab in every row, current columns
                ind   scroll text up
                ka1   upper left of keypad
                ka3   upper right of keypad
                kb2   center of keypad
                kbs   backspace key
                kc1   lower left of keypad
                kc3   lower right of keypad
                kcub1 left-arrow key
                kcud1 down-arrow key
                kcuf1 right-arrow key
                kcuu1 up-arrow key
                kent  enter/sekd key
                kf0   F0 function key
                kf1   F1 function key
                kf10  F10 function key
                kf2   F2 function key
                kf3   F3 function key
                kf4   F4 function key
                kf5   F5 function key
                kf6   F6 function key
                kf7   F7 function key
                kf8   F8 function key
                kf9   F9 function key
                rc    restore cursor to position of last save_cursor
                rev   turn on reverse video mode
                ri    scroll text down (P)
                rmacs end alternate character set
                rmam  turn off automatic margins
                rmkx  leave 'keybroad_transmit' mode
                rmso  exit standout mode
                rmul  exit underline mode
                rs2   reset string
                sc    save current cursor position (P)
                sgr   define video attribute #1-#9(PG9)
                sgr0  turn off all attributes
                smacs start alternate character set (P)
                smam  turn on automatic margins
                smkx  enter 'keyborad_transmit' mode
                smso  begin standout mode
                smul  begin underline mode
                tbc   clear all tab stops(P)
         */

                when(b.toInt()) {
                    0 -> {}
                    0x1b -> {
                        b = char
                        //System.out.println("@1: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");

                        when(b.toInt()) {
                            'M'.code -> scroll_reverse()    // sr \EM sr scroll text down (P)
                            'D'.code -> scroll_forward()    // sf
                            '7'.code -> save_cursor()
                            '('.code -> {
                                val expected = arrayOf('B'.code, 0x1b, ')'.code, '0'.code)
                                var successful = true

                                for (e in expected) {
                                    b = char
                                    if (b.toInt() != e) {
                                        pushChar(e.toByte())
                                        successful = false
                                        break
                                    }
                                }
                                if (successful)
                                    ena_acs()
                            }
                            '>'.code -> {
                                // 0x1b, '[', '?', '3', 'l', 0x1b, '[', '?', '4', 'l', 0x1b, '[', '?', '5', 'l', 0x1b, '[', '?', '7', 'h', 0x1b, '[', '?', '8', 'h'
                                for (i in 0 until 25)
                                    b = char
                                reset_2string()
                            }
                            '['.code -> {
                                print("@11: ${b.toInt().toChar()}[${Integer.toHexString((b and 0xff.toByte()).toInt())}]")
                                pushChar(b)
                            }
                            else -> {
                                //System.out.print("@2: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
                                intargi = 0
                                intarg[intargi] = 0
                                var digit = 0
                                while (true) {
                                    b = char
                                    //System.out.print("#"+new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
                                    if (b == ';'.code.toByte()) {
                                        if (digit > 0) {
                                            intargi++
                                            intarg[intargi] = 0
                                            digit = 0
                                        }
                                        continue
                                    }
                                    if ('0'.code.toByte() <= b && b <= '9'.code.toByte()) {
                                        intarg[intargi] = intarg[intargi] * 10 + (b - '0'.code.toByte())
                                        digit++
                                        continue
                                    }
                                    pushChar(b)
                                    break
                                }

                                b = char
                                //System.out.print("@4: "+ new Character((char)b)+"["+Integer.toHexString(b&0xff)+"]");
                                if (b == 'm'.code.toByte()) {
                                    /*
                                    b=getChar();
                                    if(b=='$'){
                                      b=getChar();  // <
                                      b=getChar();  // 2
                                      b=getChar();  // >
                                    }
                                    else{
                                      pushChar(b);
                                    }
                                    */
                                    if (digit == 0 && intargi == 0) {
                                        b = char
                                        if (b.toInt() == 0x0f) { // sgr0
                                            exit_attribute_mode()
                                            continue
                                        } else { // rmso, rmul
                                            exit_underline_mode()
                                            exit_standout_mode()
                                            pushChar(b)
                                            continue
                                        }
                                    }
                                    for (i in 0..intargi) {
                                        var fg: Any? = null
                                        var bg: Any? = null
                                        var tmp: Any? = null
                                        when (intarg[i]) {
                                            0 -> {
                                                exit_standout_mode()
                                                continue
                                            }
                                            1 -> {
                                                enter_bold_mode()
                                                continue
                                            }
                                            2 -> {
                                            }
                                            4 -> {
                                                enter_underline_mode()
                                                continue
                                            }
                                            5, 8 -> {
                                            }
                                            7 -> {
                                                enter_reverse_mode()
                                                continue
                                            }
                                            30, 31, 32, 33, 34, 35, 36, 37 -> {
                                                tmp = term.getColor(intarg[i] - 30)
                                                if (tmp != null) fg = tmp
                                            }
                                            40, 41, 42, 43, 44, 45, 46, 47 -> {
                                                tmp = term.getColor(intarg[i] - 40)
                                                if (tmp != null) bg = tmp
                                            }
                                            else -> {
                                            }
                                        }
                                        if (fg != null) term.setForeGround(fg)
                                        if (bg != null) term.setBackGround(bg)
                                    }
                                    //System.out.println("fg: "+fg+" bg: "+bg);
                                    continue
                                }
                                if (b == 'r'.code.toByte()) { // csr
                                    change_scroll_region(intarg[0], intarg[1])
                                    //System.out.println("r: "+region_y1+", "+region_y2+", intargi="+intargi);
                                    continue
                                }
                                if (b == 'H'.code.toByte()) { // cup
                                    /*
                                    b=getChar();
                                    if(b!='$'){      // home
                                      pushChar(b);
                                    }
                                    else{
                                      b=getChar();  // <
                                      b=getChar();  // 5
                                      b=getChar();  // >
                                    }
                                    */
                                    if (digit == 0 && intargi == 0) {
                                        intarg[1] = 1
                                        intarg[0] = intarg[1]
                                    }

                                    //System.out.println("H: "+region_y1+", "+region_y2+", intargi="+intargi);
                                    cursor_address(intarg[0], intarg[1])
                                    continue
                                }
                                if (b == 'B'.code.toByte()) { // cud
                                    parm_down_cursor(intarg[0])
                                    continue
                                }
                                if (b == 'D'.code.toByte()) { // cub
                                    parm_left_cursor(intarg[0])
                                    continue
                                }
                                if (b == 'C'.code.toByte()) { // cuf
                                    if (digit == 0 && intargi == 0) {
                                        intarg[0] = 1
                                    }
                                    parm_right_cursor(intarg[0])
                                    continue
                                }
                                if (b == 'K'.code.toByte()) { // el
                                    /*
                                    b=getChar(); //
                                    if(b=='$'){
                                      b=getChar(); // <
                                      b=getChar(); // 3
                                      b=getChar(); // >
                                    }
                                    else{
                                      pushChar(b);
                                    }
                                    */
                                    if (digit == 0 && intargi == 0) { // el
                                        clr_eol()
                                    } else { // el1
                                        clr_bol()
                                    }
                                    continue
                                }
                                if (b == 'J'.code.toByte()) {
                                    //for(int i=0; i<intargi; i++){ System.out.print(intarg[i]+" ");}
                                    //System.out.println(intarg[0]+"<- intargi="+intargi);
                                    clr_eos()
                                    continue
                                }
                                if (b == 'A'.code.toByte()) { // cuu
                                    if (digit == 0 && intargi == 0) {
                                        intarg[0] = 1
                                    }
                                    parm_up_cursor(intarg[0])
                                    continue
                                }
                                if (b == '?'.code.toByte()) {
                                    b = char
                                    if (b == '1'.code.toByte()) {
                                        b = char
                                        if (b == 'l'.code.toByte() || b == 'h'.code.toByte()) {
                                            b = char
                                            if (b.toInt() == 0x1b) {
                                                b = char
                                                if (b == '>'.code.toByte() || b == '='.code.toByte()) { // smkx   , enter 'keyborad_transmit' mode
                                                    // TODO
                                                    continue
                                                }
                                            }
                                        } else if (b == 'h'.code.toByte()) {
                                            b = char
                                            if (b.toInt() == 0x1b) {
                                                b = char
                                                if (b == '='.code.toByte()) { // smkx enter 'keyborad_transmit' mode
                                                    continue
                                                }
                                            }
                                        }
                                    } else if (b == '7'.code.toByte()) {
                                        b = char
                                        if (b == 'h'.code.toByte()) { // smam
                                            // TODO
                                            //System.out.println("turn on automatic magins");
                                            continue
                                        } else if (b == 'l'.code.toByte()) { // rmam
                                            // TODO
                                            //System.out.println("turn off automatic magins");
                                            continue
                                        }
                                        pushChar(b)
                                        b = '7'.code.toByte()
                                    }
                                }
                                if (b == 'h'.code.toByte()) { // kh \Eh home key
                                    continue
                                }
                                println(
                                    ("unknown " + Integer.toHexString((b and 0xff.toByte()).toInt()) + " "
                                            + b.toChar() + ", " + intarg[0] + ", " + intarg[1] + ", "
                                            + intarg[2] + ",intargi=" + intargi)
                                )
                            }
                        }
                    }
                    0x07 -> bell()  // bel ^G
                    0x09 -> tab()   // ht(^I)
                    0x0f -> exit_alt_charset_mode() // rmacs ^O  	// end alternate character set (P)
                    0x0e -> enter_alt_charset_mode()  // smacs ^N  	// start alternate character set (P)
                    0x0d -> carriage_return()
                    0x08 -> cursor_left()
                    0x0a -> { // '\n'
                        //System.out.println("x="+x+",y="+y);
                        cursor_down()
                        //check_region();
                    }
                    else -> {
                        pushChar(b)
                        draw_text()
                    }
                }
            }
        } catch (_: Exception) {
        }
    }


    val ENTER = byteArrayOf(0x0d)
    val UP = byteArrayOf(0x1b, 0x4f, 0x41)
    val DOWN = byteArrayOf(0x1b, 0x4f, 0x42)
    val RIGHT = byteArrayOf(0x1b, /*0x5b*/0x4f, 0x43)
    val LEFT = byteArrayOf(0x1b, /*0x5b*/0x4f, 0x44)
    val F1 = byteArrayOf(0x1b, 0x4f, 'P'.code.toByte())
    val F2 = byteArrayOf(0x1b, 0x4f, 'Q'.code.toByte())
    val F3 = byteArrayOf(0x1b, 0x4f, 'R'.code.toByte())
    val F4 = byteArrayOf(0x1b, 0x4f, 'S'.code.toByte())
    val F5 = byteArrayOf(0x1b, 0x4f, 't'.code.toByte())
    val F6 = byteArrayOf(0x1b, 0x4f, 'u'.code.toByte())
    val F7 = byteArrayOf(0x1b, 0x4f, 'v'.code.toByte())
    val F8 = byteArrayOf(0x1b, 0x4f, 'I'.code.toByte())
    val F9 = byteArrayOf(0x1b, 0x4f, 'w'.code.toByte())
    val F10 = byteArrayOf(0x1b, 0x4f, 'x'.code.toByte())
    val TAB = byteArrayOf(0x09)

    override fun getCodeENTER() = ENTER
    override fun getCodeUP() = UP
    override fun getCodeDOWN() = DOWN
    override fun getCodeRIGHT() = RIGHT
    override fun getCodeLEFT() = LEFT
    override fun getCodeF1() = F1
    override fun getCodeF2() = F2
    override fun getCodeF3() = F3
    override fun getCodeF4() = F4
    override fun getCodeF5() = F5
    override fun getCodeF6() = F6
    override fun getCodeF7() = F7
    override fun getCodeF8() = F8
    override fun getCodeF9() = F9
    override fun getCodeF10() = F10
    override fun getCodeTAB() = TAB

}