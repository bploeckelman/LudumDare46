package lando.systems.ld46.ui.typinglabel;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun;

public class TypingGlyph extends Glyph {


    /** {@link GlyphRun} this glyph belongs to. */
    public GlyphRun run = null;

    /** Internal index associated with this glyph. Internal use only. Defaults to -1. */
    int internalIndex = -1;

    /** Color of this glyph. If set to null, the run's color will be used. Defaults to null. */
    public Color color = null;
}
