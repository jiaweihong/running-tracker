package com.example.runningtracker.miscellaneous;

import android.app.Activity;
import android.app.Application;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.example.runningtracker.viewmodel.RunViewModel;
import com.example.runningtracker.R;
import com.example.runningtracker.model.Run;

import java.util.ArrayList;
import java.util.List;

public class RunAdapter extends RecyclerView.Adapter<RunAdapter.RunHolder> {
    private List<Run> runs = new ArrayList<Run>();
    private RunViewModel runViewModel;

    public RunAdapter(Application myApplication, Activity activity){
        this.runViewModel = new ViewModelProvider((ViewModelStoreOwner) activity, (ViewModelProvider.Factory) ViewModelProvider.AndroidViewModelFactory.getInstance(myApplication)).get(RunViewModel.class);
    }


    @NonNull
    @Override
    // Creates the run holder
    public RunHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.run_card, parent, false);
        return new RunHolder(itemView);
    }

    @Override
    // populates the run holder with data
    public void onBindViewHolder(@NonNull RunHolder holder, int position) {
        Run run = runs.get(position);

        holder.idTxt.setText("Run #" + run.getId());

        String distanceTxt = Math.round(run.getDistance()) + " m";
        holder.distanceTxt.setText(distanceTxt);

        holder.durationTxt.setText(Helper.secToHourMinSecString(run.getDuration()));
        holder.noteTxt.setText(run.getNote());

        holder.avgPaceTxt.setText(run.getAvgPace() + " m/s");

        holder.dateTimeTxt.setText(run.getStartDateTimeStr() + " - " + run.getEndDateTimeStr());

        holder.editNoteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // the button for each holder is used to get the application context
                AlertDialog.Builder builder = new AlertDialog.Builder(holder.editNoteBtn.getContext());
                builder.setTitle("Session note");

                // Set up the input
                final EditText input = new EditText(holder.editNoteBtn.getContext());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setText(run.getNote());

                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runViewModel.updateRunNote(run.getId(), input.getText().toString());
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });


                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return runs.size();
    }

    public void setRuns(List<Run> runs) {
        this.runs = runs;

        // notifies adapater to redraw layout
        notifyDataSetChanged();
    }

    // holds the view of each card
    class RunHolder extends RecyclerView.ViewHolder {
        private TextView durationTxt;
        private TextView distanceTxt;
        private TextView avgPaceTxt;
        private TextView dateTimeTxt;
        private TextView idTxt;
        private TextView noteTxt;
        private Button editNoteBtn;

        public RunHolder(@NonNull View itemView) {
            super(itemView);

            durationTxt = itemView.findViewById(R.id.durationTxt);
            distanceTxt = itemView.findViewById(R.id.distanceTxt);
            idTxt = itemView.findViewById(R.id.idTxt);
            noteTxt = itemView.findViewById(R.id.noteTxt);
            editNoteBtn = itemView.findViewById(R.id.editNoteBtn);
            avgPaceTxt= itemView.findViewById(R.id.avgPaceTxt);
            dateTimeTxt = itemView.findViewById(R.id.dateTimeTxt);
        }
    }
}

