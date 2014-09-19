/*  
 *  Copyright 2014 Mercury Intermedia
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.larvalabs.svgandroid;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.SparseIntArray;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Builder-style interface to SVGParser, including support for replacing multiple colors
 */
public class SVGBuilder {

    private final InputStream in;
    private SparseIntArray colorReplaceArray;
    private boolean whiteMode = false;

    private SVGBuilder(InputStream inputStream) {
        in = inputStream;
    }

    public static SVGBuilder fromInputStream(InputStream inputStream) {
        return new SVGBuilder(inputStream);
    }

    public static SVGBuilder fromString(String data) {
        return new SVGBuilder(new ByteArrayInputStream(data.getBytes()));
    }

    public static SVGBuilder fromResource(Resources resources, int resId) {
        return new SVGBuilder(resources.openRawResource(resId));
    }

    public static SVGBuilder fromAsset(AssetManager manager, String path) throws IOException {
        return new SVGBuilder(manager.open(path));
    }

    public SVGBuilder replaceColor(int searchColor, int replaceColor) {
        if (colorReplaceArray == null) colorReplaceArray = new SparseIntArray();
        colorReplaceArray.put(searchColor, replaceColor);
        return this;
    }

    public SVGBuilder setWhiteMode(boolean whiteMode) {
        this.whiteMode = whiteMode;
        return this;
    }

    public SVG buildSVG() {
        return SVGParser.parse(in, colorReplaceArray, whiteMode);
    }

    public Bitmap buildBitmap(Resources resources) {
        SVG svg = buildSVG();
        RectF bounds = svg.getBounds();
        if (bounds == null) {
            throw new IllegalStateException("buildBitmap requires bounds to be set!");
        } else {
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            float density = displayMetrics.density;

            int width = Math.round(bounds.width() * density);
            int height = Math.round(bounds.height() * density);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setDensity(displayMetrics.densityDpi);

            Canvas canvas = new Canvas(bitmap);
            canvas.scale(density, density);
            canvas.drawPicture(svg.getPicture());

            return bitmap;
        }
    }

}
