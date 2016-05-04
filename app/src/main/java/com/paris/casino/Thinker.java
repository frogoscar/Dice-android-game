package com.paris.casino;

/**
 * Created by exe on 4/05/16.
 */
public class Thinker {
    private int num[];
    public SayValue sayValue;
    private int baseCnt, baseNum;
    private boolean basePure;

    public Thinker( int num0, int num1, int num2, int num3, int num4 ) {
        num = new int[DiceActivity.MAX_DICE];
        num[0] = num0;
        num[1] = num1;
        num[2] = num2;
        num[3] = num3;
        num[4] = num4;
        sayValue = null;
        baseCnt = -1;
        baseNum = -1;
    }

    public void say() {
        sayValue = new SayValue();

        int checkCnt;
        int dstCnt = baseCnt % 100;
        int dstNum = baseNum;
        if( baseCnt < 0 ) {
            dstCnt = 2;
            dstNum = (int)( Math.random() * 5 ) + 1;
        }
        for( checkCnt = 0; checkCnt < 5; checkCnt++ ) {
            if( dstNum >= 6 ) {
                dstCnt++;
                dstNum = 2;
            } else {
                dstNum++;
            }

            int i;
            int cnt = 2;
            for( i = 0; i < DiceActivity.MAX_DICE; i++ )
                if( num[i] == dstNum )
                    cnt++;
                else if( baseCnt < 100 && !basePure && num[i] == 1 )
                    cnt++;
            if( cnt >= dstCnt ) {
                sayValue.type = SayValue.SAY_VALUE_TYPE_SAY;
                sayValue.count = dstCnt;
                sayValue.num = dstNum;
                break;
            }
        }

        if( checkCnt >= 5 ) {
            if( baseCnt < 0 ) {
                sayValue.type = SayValue.SAY_VALUE_TYPE_SAY;
                sayValue.count = 2;
                sayValue.num = 6;
            } else {
                sayValue.type = SayValue.SAY_VALUE_TYPE_OPEN;
            }
        }

        if( sayValue.type == SayValue.SAY_VALUE_TYPE_SAY )
            if( baseCnt >= 100 || basePure )
                sayValue.count += 100;
    }

    public void setBase( int cnt, int num, boolean isPure ) {
        baseCnt = cnt;
        baseNum = num;
        basePure = isPure;
    }
}
