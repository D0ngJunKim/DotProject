package kdj.dotp.widget.value;

/**
 * Created by DJKim on 2019-06-18.
 */
public enum DotOpenAnimValue {
    // Animation Values
    // 생성
    DOT_BG_TRANS(300, 0),
    DOT_BG_SCALE(300, 0),
    DOT_CLOSE(750, 100),
    DOT_LOGO(200, 0),
    BACKGROUND(200, 0),
    OUTER_LAYER(450, 0),
    OUTER_TITLE(350, 200),
    OUTER_PAGER(400, 200),
    INNER_SCALE(400, 0),
    INNER_ROTATE(400, 200);

    private int duration;
    private int delay;

    DotOpenAnimValue(int duration, int delay) {
        this.duration = duration;
        this.delay = delay;
    }

    public int getDuration() {
        return duration;
    }

    public int getDelay() {
        return delay;
    }
}