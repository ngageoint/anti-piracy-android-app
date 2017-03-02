package mil.nga.giat.asam.settings.report;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.CharacterStyle;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mil.nga.giat.asam.R;


public class ReportFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_report_fragment, container, false);

        TextView textView = (TextView) view.findViewById(R.id.report_text_with_link);
        CharSequence text = textView.getText();

        // Adapted from Linkify.addLinkMovementMethod(), to make links clickable.
        MovementMethod m = textView.getMovementMethod();
        if ((m == null) || !(m instanceof LinkMovementMethod))
        {
            textView.setMovementMethod(LinkMovementMethod.getInstance());
        }

        text = setSpanBetweenTokens(text, "$$", new ForegroundColorSpan(0xFF4444FF), new UnderlineSpan(),
                new ClickableSpan()
                {
                    @Override
                    public void onClick(View widget)
                    {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.info_fragment_report_url_text)));
                        startActivity(browserIntent);
                    }
                });

        textView.setText(text);

        return view;
    }


    /**
     * Given either a Spannable String or a regular String and a token, apply
     * the given CharacterStyle to the span between the tokens, and also remove
     * tokens.
     * <p>
     * For example, {@code setSpanBetweenTokens("Hello ##world##!", "##",
     * new ForegroundColorSpan(0xFFFF0000));} will return a CharSequence {@code
     * "Hello world!"} with {@code world} in red.
     *
     * @param text The text, with the tokens, to adjust.
     * @param token The token string; there should be at least two instances of
     *            token in text.
     * @param cs The style to apply to the CharSequence. WARNING: You cannot
     *            send the same two instances of this parameter, otherwise the
     *            second call will remove the original span.
     * @return A Spannable CharSequence with the new style applied.
     *
     * //@see http://developer.android.com/reference/android/text/style/CharacterStyle.html
     */
    public static CharSequence setSpanBetweenTokens(CharSequence text,
                                                    String token, CharacterStyle... cs)
    {
        // Start and end refer to the points where the span will apply
        int tokenLen = token.length();
        int start = text.toString().indexOf(token) + tokenLen;
        int end = text.toString().indexOf(token, start);

        if (start > -1 && end > -1)
        {
            // Copy the spannable string to a mutable spannable string
            SpannableStringBuilder ssb = new SpannableStringBuilder(text);
            for (CharacterStyle c : cs)
                ssb.setSpan(c, start, end, 0);

            // Delete the tokens before and after the span
            ssb.delete(end, end + tokenLen);
            ssb.delete(start - tokenLen, start);

            text = ssb;
        }

        return text;
    }
}
