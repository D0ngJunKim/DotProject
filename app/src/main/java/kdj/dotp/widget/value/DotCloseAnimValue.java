package kdj.dotp.widget.value;

/**
 * Created by DJKim on 2019-06-18.
 */
public enum DotCloseAnimValue {
    // Animation Values
    DOT_BG_TRANS(300, 0),
    DOT_BG_SCALE(300, 0),
    DOT_CLOSE(150, 0),
    DOT_LOGO(300, 0),
    BACKGROUND (300, 0),
    OUTER_LAYER (300, 0),
    OUTER_TITLE (300, 0),
    OUTER_PAGER (300, 0),
    INNER_SCALE (300, 0),
    INNER_ROTATE(100, 0);

    private int duration;
    private int delay;

    DotCloseAnimValue(int duration, int delay) {
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