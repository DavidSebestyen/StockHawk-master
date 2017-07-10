package com.udacity.stockhawk.ui;

import android.graphics.Color;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.EntryGraph;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by krypt on 11/04/2017.
 */

public class StockDetail extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    @BindView(R.id.stockName)
    TextView mTextView;
    @BindView(R.id.percentage_change)
    TextView mPercentageChange;
    @BindView(R.id.price_change)
    TextView mPriceChange;
    @BindView(R.id.chart)
    LineChart mGraph;

    private String stockSymbol;

    private List<EntryGraph> mEntriesOfAStock = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_detail);

        ButterKnife.bind(this);

        stockSymbol = getIntent().getStringExtra("symbol");

        getSupportLoaderManager().initLoader(0, null, this);

        mTextView.setText(stockSymbol);

    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                Contract.Quote.COLUMN_SYMBOL + "=?",
                new String[]{stockSymbol},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor stockCursor) {

        if (stockCursor == null) {
            Log.e("StockCursor", "The Stock Cursor is null");
            return;
        }

        if (stockCursor.moveToFirst()) {

            double abs_change = stockCursor.getDouble(Contract.Quote.POSITION_PERCENTAGE_CHANGE) / 100;
            String hello = String.valueOf(abs_change);


            float rawAbsoluteChange = stockCursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float percentageChange = stockCursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);
            String percentageChangeString = String.valueOf(percentageChange);
            String rawAbsoluteChangeString = String.valueOf(rawAbsoluteChange);

            if (rawAbsoluteChange > 0) {
                mPercentageChange.setTextColor(getResources().getColor(R.color.material_green_700));
                mPriceChange.setTextColor(getResources().getColor(R.color.material_green_700));
            } else {
                mPercentageChange.setTextColor(getResources().getColor(R.color.material_red_700));
                mPriceChange.setTextColor(getResources().getColor(R.color.material_red_700));
            }
            mPercentageChange.setText(percentageChangeString + "%");
            mPriceChange.setText(rawAbsoluteChangeString + "$");

            mEntriesOfAStock = getElementForTheGraph(stockCursor.getString(Contract.Quote.POSITION_HISTORY));

            createGraph();

        }

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void createGraph() {

        List<Entry> entries = new ArrayList<Entry>();

        int mEntriesOfAStockSize = mEntriesOfAStock.size();

        final String[] data = new String[mEntriesOfAStockSize];

        for (int i = 0; i < mEntriesOfAStock.size(); i++) {

            String timeStampStr = mEntriesOfAStock.get(i).getmDate();

            DateFormat sdf = new SimpleDateFormat("MM/dd");
            Date netDate = (new Date(Long.parseLong(timeStampStr)));
            String dateFormatted = sdf.format(netDate);

            data[i] = dateFormatted;
            entries.add(new Entry(i, mEntriesOfAStock.get(i).getmPrice()));

        }

        IAxisValueFormatter formatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return data[(int) value];
            }
        };


        XAxis xAxis = mGraph.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(formatter);
        xAxis.setTextSize(15f);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        YAxis yAxis = mGraph.getAxis(YAxis.AxisDependency.LEFT);
        yAxis.setTextSize(15f);
        yAxis.setTextColor(Color.WHITE);
        yAxis.setDrawGridLines(false);
        yAxis.setDrawLabels(true);

        mGraph.getLegend().setEnabled(false);
        mGraph.setDescription(null);

        LineDataSet dataSet = new LineDataSet(entries, getString(R.string.stock_price));
        LineData lineData = new LineData(dataSet);
        mGraph.setAutoScaleMinMaxEnabled(true);
        mGraph.setData(lineData);
        mGraph.invalidate();

    }



    public static List<EntryGraph> getElementForTheGraph(String entriesOfAStock) {
        List<EntryGraph> list = new ArrayList<>();

        String[] lineDivider = entriesOfAStock.split("\n");

        for (String line : lineDivider) {
            String[] commaDivider = line.split(",");
            String date = commaDivider[0];
            float price = Float.parseFloat(commaDivider[1]);
            list.add(new EntryGraph(date, price));
        }
        return list;
    }
}
