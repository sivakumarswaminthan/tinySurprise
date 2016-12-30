package com.kratav.tinySurprise.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;


import com.kratav.tinySurprise.R;
import com.kratav.tinySurprise.activities.HomeActivity;
import com.kratav.tinySurprise.adapter.ProductGridAdapter;
import com.kratav.tinySurprise.bean.CustomOptions;
import com.kratav.tinySurprise.bean.Product;
import com.kratav.tinySurprise.contants.AppConstant;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class RetriveProductAccordingToCategory extends
        AsyncTask<String, Product, String> {

    protected ProgressDialog dialog;
    private Activity context;
    private ProductGridAdapter pa;
    private List<Product> productList;
    private TextView footerTv;

    private static final String url_address = "http://tinysurprise.com/test_mode/mobi-app/cat.php?category_name=";

    public RetriveProductAccordingToCategory(Activity context, ProductGridAdapter pa, List<Product> productList) {
        this.context = context;
        this.pa = pa;
        this.productList = productList;

    }

    public RetriveProductAccordingToCategory() {


    }

    @SuppressLint("NewApi")
    @Override
    protected void onPreExecute() {
        if (((HomeActivity) context).footer.getVisibility() == View.GONE) {
            dialog = new ProgressDialog(context, R.style.transparentDialog);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            dialog.setContentView(R.layout.progress);
            dialog.findViewById(R.id.loading_icon).startAnimation(AnimationUtils.loadAnimation(context, R.anim.anim_rotate));

        }else {
            footerTv = (TextView) (((HomeActivity) context).footer.findViewById(R.id.footerTextView));
            footerTv.setText("Loading...");
        }
        //productList.clear();
        pa.notifyDataSetChanged();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onProgressUpdate(Product... values) {
        super.onProgressUpdate(values);
        Product productsDS = values[0];
        productList.add(productsDS);
        pa.notifyDataSetChanged();
    }


    @Override
    protected String doInBackground(String... aloc) {

        String urlx;
        URL url = null;
        try {
            urlx = url_address + aloc[0] + "&page=" + aloc[1] + "&limit=10";
            url = new URL(urlx);
            URI uri = new URI(url.getProtocol(), url.getUserInfo(),
                    url.getHost(), url.getPort(), url.getPath(),
                    url.getQuery(), url.getRef());
            url = uri.toURL();
            Log.e("ProductCategoryAsyn", url.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet(url.toString());
            HttpResponse response = client.execute(request);
            StatusLine statusLine = response.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode == 200) {
                HttpEntity entity = response.getEntity();
                int count;
                if (entity != null) {
                    InputStream instream = entity.getContent();
                    String jsonResult = RestClient
                            .convertStreamToString(instream);
                    Log.e("Retrive prodct category", "" + jsonResult);

                    if (jsonResult == null || jsonResult.equalsIgnoreCase("null")) {
                        return null;
                    } else {

                        JSONArray ja = new JSONArray(jsonResult).getJSONArray(0);
                        System.out.println("By Category: " + ja.length() + " : " + ja);

                        JSONArray jx = ja.getJSONObject(0).getJSONArray("data");
                        for (count = 0; count <= jx.length() - 1; count++) {

                            Product productsDS = new Product();
                            JSONObject x = jx.getJSONObject(count);
                            try {
                                productsDS.setProductName(x.getString("Product name"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setProductCode(x.getString("Product code"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                int isInStock = Integer.parseInt(x.getString("is_in_stock"));
                                productsDS.setIsInStock(isInStock == 1 ? true : false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setViewCount(x.getInt("Viewcount"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setProductDescription(x
                                        .getString("Description"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setProductSDescription(x
                                        .getString("Short_description"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                String load = x.getString("Price");


                                load = load.substring(0, load.indexOf("."));
                                String str = load.replaceAll("[^\\d.]", "");
                                int price = Integer.parseInt(str);

                                productsDS.setProductPrice(price);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setShippingCharges(x.getString("Shipping charges"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setProductImageUrl(x.getString("Thumbnail Image"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            // Log.e("Url is: ", "" +
                            // productsDS.getProductImageUrl());
                            try {
                                productsDS.setAvailableCities(x.getString("City"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                productsDS.setTimeToDeliver(x.getString("Delivery Date"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            List<String> images = new ArrayList<String>();
                            try {
                                JSONObject galleryImages = x
                                        .getJSONObject("images");
                                JSONArray galleryImageArray = galleryImages.getJSONArray("Base Image");

                                for (int i = 0; i < galleryImageArray.length(); i++) {
                                    images.add(galleryImageArray.getString(i));
                                    System.out.println(images);
                                }
                            } catch (Exception e) {
                                // e.printStackTrace();
                                images.add("http://www.stevescheese.com/wp-content/plugins/woocommerce/assets/images/no_product_placeholder.png");
                            }
                            productsDS.setGalleryImageUrlList(images);

                            try {

                                for (int i = 0; i < x.getJSONArray("Custom Title").length(); i++) {
                                    CustomOptions co = new CustomOptions();
                                    co.setMsgTitle(x.getJSONArray("Custom Title").getString(i).toString());
                                    productsDS.getCustomOptions().add(co);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                for (int i = 0; i < productsDS.getCustomOptions().size(); i++)
                                    if (x.getJSONArray("Custom Require ").getInt(i) == 1)
                                        productsDS.getCustomOptions().get(i).setMendatory(true);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                for (int i = 0; i < productsDS.getCustomOptions().size(); i++) {
                                    productsDS.getCustomOptions().get(i).setMsgType(x.getJSONArray("Custom Type ").getString(i));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                System.out.println(x.getString("Custom Values "));
                                String[] s = x.getString("Custom Values ").split(",");
                                for (int i = 0; i < s.length; i++) {
                                    productsDS.getCustomOptions().get(i).setMsgValue(s[i]);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            try {
                                System.out.println(x.getString("Custom Price"));
                                String[] s = x.getString("Custom Price").split(",");
                                for (int i = 0; i < s.length; i++) {
                                    productsDS.getCustomOptions().get(i).setCustomPrice(s[i]);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            publishProgress(productsDS);

                            if (dialog != null && dialog.isShowing() && count == 7) {
                                dialog.dismiss();
                            }
                        }
                    }
                }
            }
            return "Success";
        } catch (SocketTimeoutException e) {
            return "Err: Connection Timeout. \n\nPlease make sure your internet connection is working.";
        } catch (UnknownHostException e) {
            return "Err: Connection Timeout. \n\nPlease make sure your internet connection is working.";
        } catch (ConnectException e) {
            return "Err: Connection Timeout. \n\nPlease make sure your internet connection is working.";
        } catch (org.json.JSONException je) {
            return "Err: Internal Server Exception.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Err: Some Prob. \n\n" + e.getMessage() + ".";
        } finally {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (productList.size() < 1 && (result == null || result.equals(""))) {
            ((HomeActivity) context).showErrorLayout("No product available in this category.");
        } else if (result != null && result.equals("Err: Internal Server Exception.")) {
            ((HomeActivity) context).showErrorLayout("Internal Server Exception.");
        } else if (result != null && result.contains("Err: Connection Timeout")) {
            ((HomeActivity) context).showErrorLayout("Please check your internet connection.");
        } else if (((HomeActivity) context).footer.getVisibility() == View.GONE) {
            ((HomeActivity) context).toggleFooterViewVisibilty();
            if(productList.size()<10) footerTv.setText(AppConstant.NO_PRODUCT_AVAIL);
        } else if (footerTv != null) {
            if (productList.size() >= 10 && productList.size() % 10 == 0) {
                footerTv.setText(AppConstant.LOAD_MORE);
            } else
                footerTv.setText(AppConstant.NO_PRODUCT_AVAIL);
        }
    }
}