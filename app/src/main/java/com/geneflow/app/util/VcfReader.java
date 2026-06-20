package com.geneflow.app.util;

import android.content.Context;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/** Reads VCF text from a content Uri (file picker) or from the bundled asset. */
public class VcfReader {

    /** Read the whole text of a picked file. Returns null on failure. */
    public static String readFromUri(Context context, Uri uri) {
        if (uri == null) return null;
        try (InputStream is = context.getContentResolver().openInputStream(uri)) {
            return readStream(is);
        } catch (Exception e) {
            return null;
        }
    }

    /** Read the bundled sample so the demo always has data to analyse. */
    public static String readSample(Context context) {
        try (InputStream is = context.getAssets().open("sample.vcf")) {
            return readStream(is);
        } catch (Exception e) {
            return "";
        }
    }

    private static String readStream(InputStream is) throws Exception {
        if (is == null) return null;
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }
}
