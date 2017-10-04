package uk.tanton.subtitles.live;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.args4j.Option;

public class Args {

    @Option(name = "--bgcolour", usage = "Background colour CSS e.g. #0f0", required = false) private String bgColour;
    @Option(name = "--textboxo", usage = "Opacity of textbox background (between 0 and 1)", required = false) private String textBoxOpacity;

    public String getBgColour() {
        if (StringUtils.isEmpty(bgColour)) {
            return "#0f0";
        }

        return bgColour;
    }

    public String getTextBoxOpacity() {
        if (StringUtils.isEmpty(textBoxOpacity)) {
            return "0";
        }

        Double a = Double.valueOf(textBoxOpacity);
        if (a < 0 || a > 1) {
            throw new IllegalArgumentException(String.format("Text box opactiy must be between 0 and 1. Illegal value of %s", a));
        }
        return textBoxOpacity;
    }
}
