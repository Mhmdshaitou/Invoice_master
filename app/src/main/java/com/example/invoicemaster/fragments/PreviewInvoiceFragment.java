package com.example.invoicemaster.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.invoicemaster.InvoiceItem;
import com.example.invoicemaster.Item;
import com.example.invoicemaster.R;
import com.example.invoicemaster.SharedViewModelPreview;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class PreviewInvoiceFragment extends Fragment {

    private PdfDocument pdfDocument;
    private static final String FILE_PROVIDER_AUTHORITY = "com.example.invoicemaster.fileprovider";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_preview_invoice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedViewModelPreview sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModelPreview.class);

        sharedViewModel.getInvoiceItems().observe(getViewLifecycleOwner(), invoiceItems -> {
            if (invoiceItems != null && !invoiceItems.isEmpty()) {
                Log.d("PreviewInvoiceFragment", "Number of items received: " + invoiceItems.size());
                generateInvoiceItems(view, invoiceItems);
            } else {
                Log.d("PreviewInvoiceFragment", "Invoice items list is empty or null");
            }
        });

        sharedViewModel.getInvoiceId().observe(getViewLifecycleOwner(), newInvoiceId -> {
            TextView invoiceIdOutputTextView = view.findViewById(R.id.idoutput);
            invoiceIdOutputTextView.setText(newInvoiceId);
        });

        sharedViewModel.getClientName().observe(getViewLifecycleOwner(), newClientName -> {
            TextView clientOutputTextView = view.findViewById(R.id.clientoutput);
            clientOutputTextView.setText(newClientName);
        });

        sharedViewModel.getClientAddress().observe(getViewLifecycleOwner(), newClientAddress -> {
            TextView addressTextView = view.findViewById(R.id.address);
            addressTextView.setText(newClientAddress);
        });

        sharedViewModel.getClientPhone().observe(getViewLifecycleOwner(), newClientPhone -> {
            TextView phoneTextView = view.findViewById(R.id.phone);
            phoneTextView.setText(newClientPhone);
        });

        sharedViewModel.getSubtotal().observe(getViewLifecycleOwner(), newSubtotal -> {
            TextView subtotalView = view.findViewById(R.id.subtotalValue);
            subtotalView.setText(formatWithSpace(newSubtotal) + " CFA");
        });

        sharedViewModel.getDiscount().observe(getViewLifecycleOwner(), newDiscount -> {
            TextView discountView = view.findViewById(R.id.discountValue);
            discountView.setText(formatWithSpace(newDiscount) + " CFA");
        });

        sharedViewModel.getTax().observe(getViewLifecycleOwner(), newTax -> {
            TextView taxView = view.findViewById(R.id.taxValue);
            taxView.setText(formatWithSpace(newTax) + " CFA");
        });

        sharedViewModel.getTotal().observe(getViewLifecycleOwner(), newTotal -> {
            TextView totalView = view.findViewById(R.id.totalValue);
            totalView.setText(formatWithSpace(newTotal) + " CFA");
        });

        // Button to print RelativeLayout
        Button printButton = view.findViewById(R.id.printbutton);
        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String clientName = ((TextView) view.findViewById(R.id.clientoutput)).getText().toString();
                printRelativeLayout(view, clientName);
            }
        });
        // Button to share PDF
        Button shareButton = view.findViewById(R.id.sharebutton);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareInvoice(view);
            }
        });
    }

    private String formatWithSpace(double value) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.FRANCE);
        DecimalFormat format = new DecimalFormat("#,##0", symbols);
        return format.format(value);
    }

    private void generateInvoiceItems(View view, List<InvoiceItem> invoiceItems) {
        LinearLayout itemsLayout = view.findViewById(R.id.itemsContainer);
        itemsLayout.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        Log.d("PreviewInvoiceFragment", "Generating invoice items, count: " + invoiceItems.size());

        for (int i = 0; i < invoiceItems.size(); i++) {
            InvoiceItem invoiceItem = invoiceItems.get(i);
            View itemView = inflater.inflate(R.layout.invoice_item_view, itemsLayout, false);

            // Bind data to your views
            TextView itemNameView = itemView.findViewById(R.id.itemname);
            TextView code = itemView.findViewById(R.id.itemcode);
            TextView quantityView = itemView.findViewById(R.id.itemunit);
            TextView itemNUView = itemView.findViewById(R.id.unitnumber);
            TextView itemPrixView = itemView.findViewById(R.id.itemprice);
            TextView itemTotalView = itemView.findViewById(R.id.totalprice);

            itemNameView.setText(invoiceItem.getItemName());
            code.setText(invoiceItem.getCode());
            quantityView.setText(String.valueOf(invoiceItem.getUnitqty()));
            itemNUView.setText(String.valueOf(invoiceItem.getNu()));
            itemPrixView.setText(formatWithSpace(invoiceItem.getItemSinglePrice()));
            itemTotalView.setText(formatWithSpace(invoiceItem.getItemTotal()));

            // Alternate row color: odd rows get blue background
            if (i % 2 == 0) {
                itemView.setBackgroundColor(getResources().getColor(R.color.lightblue));
            } else {
                itemView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }

            itemsLayout.addView(itemView);
        }
    }



    private void printRelativeLayout(View view, String clientName) {
        // Define A4 page size in pixels
        int a4WidthPx = 600;  // A4 width at 72 DPI
        int a4HeightPx = 762; // A4 height at 72 DPI

        // Get a reference to the RelativeLayout
        RelativeLayout layoutToPrint = view.findViewById(R.id.invoice);

        // Calculate scaling factors for width and height
        float scaleX = (float) a4WidthPx / layoutToPrint.getWidth();

        // Apply scaling factor to the layout width
        int newWidth = (int) (layoutToPrint.getWidth() * scaleX);

        // Create a PdfDocument with the A4 page dimensions
        PdfDocument pdfDocument = new PdfDocument(); // Initialize PdfDocument object

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(newWidth, a4HeightPx, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        // Draw the layout on the canvas
        Canvas canvas = page.getCanvas();
        canvas.scale(scaleX, scaleX); // Scale canvas to match layout width
        layoutToPrint.draw(canvas);

        pdfDocument.finishPage(page);
        try {
            // Sanitize the client name to create a valid filename
            String sanitizedClientName = clientName.replaceAll("[^a-zA-Z0-9]", "_");
            String filename = sanitizedClientName + "_Invoice.pdf";
            FileOutputStream outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
            pdfDocument.writeTo(outputStream);
            outputStream.close();

            PrintManager printManager = (PrintManager) getActivity().getSystemService(Context.PRINT_SERVICE);
            printManager.print("Invoice Print", new PrintDocumentAdapter() {

                @Override
                public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                     CancellationSignal cancellationSignal, LayoutResultCallback callback,
                                     Bundle metadata) {
                    if (cancellationSignal.isCanceled()) {
                        callback.onLayoutCancelled();
                        return;
                    }

                    PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(filename)
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .build();

                    callback.onLayoutFinished(pdi, true);
                }

                @Override
                public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                    CancellationSignal cancellationSignal, WriteResultCallback callback) {
                    try {
                        ParcelFileDescriptor.AutoCloseOutputStream fileOutputStream =
                                new ParcelFileDescriptor.AutoCloseOutputStream(destination);
                        FileInputStream fileInputStream = new FileInputStream(getContext().getFileStreamPath(filename));

                        byte[] buf = new byte[1024];
                        int bytesRead;

                        while ((bytesRead = fileInputStream.read(buf)) > 0) {
                            fileOutputStream.write(buf, 0, bytesRead);
                        }

                        callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});

                    } catch (IOException e) {
                        callback.onWriteFailed(e.toString());
                    }
                }
            }, null);

        } catch (IOException e) {
            Log.e("PreviewInvoiceFragment", "Error writing PDF", e);
        } finally {
            // Close the document
            pdfDocument.close();
        }
    }


    private void shareInvoice(View view) {
        // Use PrintManager to print the invoice as a PDF
        PrintManager printManager = (PrintManager) getContext().getSystemService(Context.PRINT_SERVICE);

        // Get a reference to the RelativeLayout to be printed
        RelativeLayout layoutToPrint = view.findViewById(R.id.invoice);

        // Specify a file name and location
        String clientName = ((TextView) view.findViewById(R.id.clientoutput)).getText().toString();
        String sanitizedClientName = clientName.replaceAll("[^a-zA-Z0-9]", "_");
        String filename = sanitizedClientName + "_Invoice.pdf";
        File filePath = new File(getContext().getFilesDir(), filename);

        // Create a PrintDocumentAdapter to manage the printing process
        PrintDocumentAdapter printAdapter = new PrintDocumentAdapter() {
            @Override
            public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                                 CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
                if (cancellationSignal.isCanceled()) {
                    callback.onLayoutCancelled();
                    return;
                }

                PrintDocumentInfo info = new PrintDocumentInfo.Builder(filename)
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .build();
                callback.onLayoutFinished(info, true);
            }

            @Override
            public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
                                CancellationSignal cancellationSignal, WriteResultCallback callback) {
                try {
                    // Create a PDF document from the layout
                    PdfDocument pdfDocument = new PdfDocument();
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                            layoutToPrint.getWidth(), layoutToPrint.getHeight(), 1).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                    Canvas canvas = page.getCanvas();
                    layoutToPrint.draw(canvas);
                    pdfDocument.finishPage(page);

                    // Write the PDF document to the specified file
                    FileOutputStream output = new FileOutputStream(filePath);
                    pdfDocument.writeTo(output);
                    output.close();
                    pdfDocument.close();

                    // Pass the document to the print manager
                    ParcelFileDescriptor.AutoCloseOutputStream out =
                            new ParcelFileDescriptor.AutoCloseOutputStream(destination);
                    FileInputStream in = new FileInputStream(filePath);

                    byte[] buf = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(buf)) > 0) {
                        out.write(buf, 0, bytesRead);
                    }

                    callback.onWriteFinished(pages);

                    // Share the PDF after it's written
                    sharePdf(filePath);

                } catch (IOException e) {
                    Log.e("PreviewInvoiceFragment", "Error creating PDF", e);
                    callback.onWriteFailed(e.toString());
                }
            }
        };

        // Trigger the print dialog and automatically save the PDF
        PrintAttributes.Builder builder = new PrintAttributes.Builder();
        builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4);
        builder.setResolution(new PrintAttributes.Resolution("pdf", "pdf", 600, 600));
        builder.setMinMargins(PrintAttributes.Margins.NO_MARGINS);

        printManager.print(filename, printAdapter, builder.build());
    }

    private void sharePdf(File pdfFile) {
        Uri uri = FileProvider.getUriForFile(getContext(), FILE_PROVIDER_AUTHORITY, pdfFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Invoice");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "Share Invoice"));
    }


}
