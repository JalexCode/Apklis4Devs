package com.jalexcode.apklis4devsmanager.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.jalexcode.apklis4devsmanager.R;
import com.jalexcode.apklis4devsmanager.util.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cu.kareldv.apklis.api2.Session;
import cu.kareldv.apklis.api2.model.Sale;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.LineChartView;

public class SalesActivity extends AppCompatActivity {
    private final int MONEY_AMMOUNT = 0;
    private final int SALES_AMMOUNT = 1;
    private final String INITIAL_DATE = "INITIAL_DATE";
    private final String FINAL_DATE = "FINAL_DATE";

    private List<Sale> sales;
    Session session;

    private LineChartView moneyAndSalesAmmountChart;
    private LineChartData lineData;

    private ImageView backButton, filterButton;
    private TextView initialDateText, finalDateText, salesCountText, totalMoneyCountText;
    private SwitchCompat switch1, switch2;

    private DatePickerDialog datePicker;
    private Date initFilterDate;
    private Date finalFilterDate;

    public final static String[] months = new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
            "Sep", "Oct", "Nov", "Dec",};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales);
        //
        setUpUI();
        //
        Intent currentSession = getIntent();
        if (currentSession != null) {
            if (currentSession.hasExtra("session")) {
                session = (Session) currentSession.getSerializableExtra("session");
                if (session != null) {
                    //
                    new getSalesThread().execute();
                }
            }
        }
    }

    private void setUpUI() {
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> onBackPressed());
        //
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        switch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchSalesOrMoneyAmmount(SALES_AMMOUNT);
                } else {
                    switchSalesOrMoneyAmmount(MONEY_AMMOUNT);
                }
            }
        });
        //
        filterButton = findViewById(R.id.filterButton);
        filterButton.setOnClickListener(v -> new getSalesThread().execute());//initLineData(initFilterDate, finalFilterDate)
        //
        initialDateText = findViewById(R.id.initialDateText);
        initialDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDate(INITIAL_DATE);
            }
        });
        finalDateText = findViewById(R.id.finalDateText);
        finalDateText.setOnClickListener(v -> setDate(FINAL_DATE));
        //
        salesCountText = findViewById(R.id.salesCountText);
        totalMoneyCountText = findViewById(R.id.totalMoneyCountText);
        //
        moneyAndSalesAmmountChart = findViewById(R.id.moneyAndSalesAmmountChart);
        // current date
        Calendar cldr = Calendar.getInstance();
        initFilterDate = new Date(cldr.get(Calendar.YEAR) - 1900, cldr.get(Calendar.MONTH), 1);
        finalFilterDate = new Date(cldr.get(Calendar.YEAR) - 1900, cldr.get(Calendar.MONTH), cldr.get(Calendar.DAY_OF_MONTH));
//        String date = cldr.get(Calendar.DAY_OF_MONTH) + "/" + (cldr.get(Calendar.MONTH) + 1) + "/" + cldr.get(Calendar.YEAR);
        initialDateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(initFilterDate));
        finalDateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(finalFilterDate));
    }

    private void setDate(String which) {
        Calendar cldr = Calendar.getInstance();
        int day = cldr.get(Calendar.DAY_OF_MONTH);
        int month = cldr.get(Calendar.MONTH);
        int year = cldr.get(Calendar.YEAR);
        Date d;
        if (which.equals(INITIAL_DATE)) {
            if (initFilterDate != null) {
                d = initFilterDate;
                day = d.getDate();
                month = d.getMonth();
                year = d.getYear() + 1900;
            }
        } else {
            if (finalFilterDate != null) {
                d = finalFilterDate;
                day = d.getDate();
                month = d.getMonth();
                year = d.getYear() + 1900;
            }
        }


        datePicker = new DatePickerDialog(SalesActivity.this, (view, year1, month1, dayOfMonth) -> {
            if (which.equals(INITIAL_DATE)) {
                initFilterDate = new Date(year1 - 1900, month1, dayOfMonth);
            } else {
                finalFilterDate = new Date(year1 - 1900, month1, dayOfMonth);
            }
            // DatePickerDialog API returns month as index, not like real month number. August is 8th month, then DatePickerDialog returns it as 7.
            // Solution: add 1 to month
            if (which.equals(INITIAL_DATE)) {
                initialDateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(initFilterDate));
            } else {
                finalDateText.setText(new SimpleDateFormat("dd/MM/yyyy").format(finalFilterDate));
            }
        }, year, month, day);
        datePicker.getDatePicker().setMaxDate(cldr.getTimeInMillis());
        datePicker.show();
    }

    private void setSaleStatsData() {
        int totalSales = 0;
        double totalMoney = 0;
        for (Sale sale : sales) {
            totalSales += sale.getSales();
            totalMoney += sale.getAmmount();
        }
        totalMoney = Util.getDiscount(totalMoney);
        salesCountText.setText(String.valueOf(totalSales));
        totalMoneyCountText.setText(String.format("%.2f CUP", totalMoney));

    }

    private void initLineData(Date initialDate, Date finalDate) {
        List<AxisValue> axisLeftValues = new ArrayList<AxisValue>();
        if (lineData == null) {
            //
            List<PointValue> ammountValues = new ArrayList<PointValue>();
            int i = 0;
            for (int j = 0; j < sales.size(); ++j) {
                // date
                Date date = sales.get(i).getDate();
                Log.e("CURRENT DATE", new SimpleDateFormat("dd-MM-yyyy").format(date));
                Log.e("INIT DATE", new SimpleDateFormat("dd-MM-yyyy").format(initialDate));
                Log.e("FINAL DATE", new SimpleDateFormat("dd-MM-yyyy").format(finalDate));
                Log.e("INIT RESULT", String.valueOf(date.compareTo(initialDate)));
                Log.e("FINAL RESULT", String.valueOf(date.compareTo(finalDate)));
                //            if (date.compareTo(initialDate) >= 0 && date.compareTo(finalDate) <= 0) {
                Log.e("ACCEPTED DATE", "OK");
                // values
                ammountValues.add(new PointValue(i, sales.get(j).getAmmount()));
                // date left axis
                String fDate = new SimpleDateFormat("dd MMM").format(date); //yyyy-MM-dd
                axisLeftValues.add(new AxisValue(i).setLabel(fDate));
                i++;
                Log.e("i", String.valueOf(i));
                //            }
            }

            Log.e("axisLeftValues SIZE", String.valueOf(axisLeftValues.size()));
            Log.e("ammountValues SIZE", String.valueOf(ammountValues.size()));

            Line ammountLine = new Line(ammountValues);
            ammountLine.setColor(ChartUtils.COLOR_GREEN).setCubic(false);

            List<Line> lines = new ArrayList<Line>();
            lines.add(ammountLine);

            lineData = new LineChartData(lines);
            lineData.setAxisXBottom(new Axis(axisLeftValues).setHasLines(true));
            lineData.setAxisYLeft(new Axis().setHasLines(true));//.setMaxLabelChars(3));

            moneyAndSalesAmmountChart.setLineChartData(lineData);
            // For build-up animation you have to disable viewport recalculation.
            moneyAndSalesAmmountChart.setViewportCalculationEnabled(false);

            // And set initial max viewport and current viewport- remember to set viewports after data.
            Viewport v = new Viewport(0, 200, 7, 0);
//        chartTop.setMaximumViewport(v);
            moneyAndSalesAmmountChart.setCurrentViewport(v);
            //
            moneyAndSalesAmmountChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        }else{
            axisLeftValues.clear();
            // Modify data targets
            Line line = lineData.getLines().get(0);// For this example there is always only one line.
            line.setColor(ChartUtils.COLOR_GREEN);
            int i = 0;
            for (PointValue value : line.getValues()) {
                // Change target only for Y value.
                value.setTarget(value.getX(), sales.get(i).getAmmount());
                // date left axis
                Date date = sales.get(i).getDate();
                String fDate = new SimpleDateFormat("dd MMM").format(date); //yyyy-MM-dd
                axisLeftValues.add(new AxisValue(i).setLabel(fDate));

                i++;
            }
            lineData.setAxisXBottom(new Axis(axisLeftValues).setHasLines(true));
        }
        moneyAndSalesAmmountChart.startDataAnimation(300);
    }

    private void switchSalesOrMoneyAmmount(int type) {
        int color = type == 0 ? ChartUtils.COLOR_GREEN : ChartUtils.COLOR_RED;

        // Cancel last animation if not finished.
        moneyAndSalesAmmountChart.cancelDataAnimation();

        // Modify data targets
        Line line = lineData.getLines().get(0);// For this example there is always only one line.
        line.setColor(color);
        int i = 0;
        for (PointValue value : line.getValues()) {
            // Change target only for Y value.
            value.setTarget(value.getX(), type == 0 ? sales.get(i).getAmmount() : sales.get(i).getSales());
            i++;
        }

        // And set initial max viewport and current viewport- remember to set viewports after data.
        Viewport v = new Viewport(0, 100, 7, 0);
//        chartTop.setMaximumViewport(v);
        moneyAndSalesAmmountChart.setCurrentViewport(v);

        // Start new data animation with 300ms duration;
        moneyAndSalesAmmountChart.startDataAnimation(300);
    }

    private class getSalesThread extends AsyncTask<Void, Void, Void> { //AsyncTask<File, Void, TreeNode<DataNode>>
        private String error = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            if (!error.isEmpty()) {
                new ErrorDialog(error);
                return;
            }
            //
            initLineData(initFilterDate, finalFilterDate);
            setSaleStatsData();
            //
//            progressHUD.dismiss();
        }

        public Void doInBackground(Void... voids) {
            try {
                Log.e("REQUESTING DATA", "ON THREAD");
                sales = session.getSales(initFilterDate, finalFilterDate, session.getUserInfo().getPhone(), switch1.isChecked() ? "month" : "day");
                Log.e("REQUESTING DATA", "OK");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public class ErrorDialog {
        public ErrorDialog(String ex) {
            StringBuilder errorMessage = new StringBuilder();
            //
            errorMessage.append(ex);
            //
            AlertDialog.Builder errorMessageDialog = new AlertDialog.Builder(SalesActivity.this);
            errorMessageDialog.setCancelable(true)
                    .setMessage(errorMessage)
                    .setTitle("Ha ocurrido un error")
                    .setIcon(R.drawable.ic_error)
                    .setPositiveButton("Copiar", (dialogInterface, i) -> {
                        ClipboardManager clipboard = (ClipboardManager) SalesActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("feedback", errorMessage.toString());
                        clipboard.setPrimaryClip(clip);
                        Toast.makeText(SalesActivity.this, "Registro copiado al portapapeles", Toast.LENGTH_SHORT).show();
//                            Intent share = new Intent(Intent.ACTION_SEND);
//                            share.setType("text/plain");
//                            share.putExtra(Intent.EXTRA_SUBJECT, "Registro de actividades de Portal Usuario");
//                            share.putExtra(Intent.EXTRA_TEXT, log);
//                            startActivity(Intent.createChooser(share, "Enviar registro de Portal Usuario"));
                    })
                    .setNegativeButton("Atrás", null);
            errorMessageDialog.show();
        }

    }
}