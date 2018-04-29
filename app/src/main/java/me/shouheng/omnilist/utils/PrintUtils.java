package me.shouheng.omnilist.utils;

import android.content.Context;
import android.print.PrintManager;
import android.webkit.WebView;

/**
 * Created by wang shouheng on 2017/12/28.*/
public class PrintUtils {

    public static void print(Context ctx, WebView webView, String title) {
        if (PalmUtils.isKitKat()) {
            PrintManager printManager = (PrintManager) ctx.getSystemService(Context.PRINT_SERVICE);
            if (PalmUtils.isLollipop()) {
                printManager.print("Print_Assignment", webView.createPrintDocumentAdapter(title), null);
                return;
            }
            printManager.print("Print_Assignment", webView.createPrintDocumentAdapter(), null);
        }
    }
}
