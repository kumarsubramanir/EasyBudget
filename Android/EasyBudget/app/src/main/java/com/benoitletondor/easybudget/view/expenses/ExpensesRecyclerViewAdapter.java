package com.benoitletondor.easybudget.view.expenses;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.benoitletondor.easybudget.R;
import com.benoitletondor.easybudget.model.Expense;
import com.benoitletondor.easybudget.model.db.DB;
import com.benoitletondor.easybudget.view.ExpenseEditActivity;
import com.benoitletondor.easybudget.view.MainActivity;

import java.util.Date;
import java.util.List;

/**
 * @author Benoit LETONDOR
 */
public class ExpensesRecyclerViewAdapter extends RecyclerView.Adapter<ExpensesRecyclerViewAdapter.ViewHolder>
{
    private List<Expense> expenses;
    private Date          date;
    private Activity  activity;
    private DB db;

    public ExpensesRecyclerViewAdapter(Activity activity, DB db, Date date)
    {
        if (db == null)
        {
            throw new NullPointerException("db==null");
        }

        if (date == null)
        {
            throw new NullPointerException("date==null");
        }

        if( activity == null )
        {
            throw new NullPointerException("activity==null");
        }

        this.activity = activity;
        this.date = date;
        this.expenses = db.getExpensesForDay(date);
        this.db = db;
    }

    /**
     * Return the date content is displayed for displayed
     *
     * @return
     */
    public Date getDate()
    {
        return date;
    }

// ------------------------------------------>

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i)
    {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycleview_expense_cell, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i)
    {
        final Expense expense = expenses.get(i);

        viewHolder.expenseTitleTextView.setText(expense.getTitle());
        viewHolder.expenseAmountTextView.setText(-expense.getAmount()+" €");
        viewHolder.monthlyIndicator.setVisibility(expense.isMonthly() ? View.VISIBLE : View.GONE);
        viewHolder.positiveIndicator.setImageResource(expense.getAmount() < 0 ? R.drawable.ic_label_green : R.drawable.ic_label_red);

        viewHolder.view.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if( !expense.isMonthly() )
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle(R.string.dialog_edit_expense_title);
                    builder.setItems(R.array.dialog_edit_expense_choices, new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int which)
                        {
                            switch (which)
                            {
                                case 0: // Edit expense
                                {
                                    Intent startIntent = new Intent(viewHolder.view.getContext(), ExpenseEditActivity.class);
                                    startIntent.putExtra("date", expense.getDate());
                                    startIntent.putExtra("expense", expense);

                                    ActivityCompat.startActivityForResult(activity, startIntent, MainActivity.ADD_EXPENSE_ACTIVITY_CODE, null);
                                }
                                case 1: // Delete
                                {
                                    if ( db.deleteExpense(expense) )
                                    {
                                        // Send notification to inform views that this expense has been deleted
                                        Intent intent = new Intent(MainActivity.INTENT_EXPENSE_DELETED);
                                        intent.putExtra("expense", expense);
                                        LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(intent);
                                    }
                                }
                            }
                        }
                    });
                    builder.show();
                }

            }
        });
    }

    @Override
    public int getItemCount()
    {
        return expenses.size();
    }

// ------------------------------------------->

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        public final TextView expenseTitleTextView;
        public final TextView expenseAmountTextView;
        public final ViewGroup monthlyIndicator;
        public final ImageView positiveIndicator;
        public final View view;

        public ViewHolder(View v)
        {
            super(v);

            view = v;
            expenseTitleTextView = (TextView) v.findViewById(R.id.expense_title);
            expenseAmountTextView = (TextView) v.findViewById(R.id.expense_amount);
            monthlyIndicator = (ViewGroup) v.findViewById(R.id.monthly_indicator);
            positiveIndicator = (ImageView) v.findViewById(R.id.positive_indicator);
        }
    }
}