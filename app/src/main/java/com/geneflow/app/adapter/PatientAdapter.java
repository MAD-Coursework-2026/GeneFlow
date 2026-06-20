package com.geneflow.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.geneflow.app.R;
import com.geneflow.app.model.Patient;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds {@link Patient} rows into the dashboard RecyclerView (item_patient.xml).
 * The coloured status dot is driven by {@link Patient#getStatus()}.
 */
public class PatientAdapter extends RecyclerView.Adapter<PatientAdapter.VH> {

    /** Callbacks for the per-row actions shown in the Figma design. */
    public interface Listener {
        void onOpenProfile(Patient p);
        void onUpload(Patient p);
        void onFamily(Patient p);
        void onDelete(Patient p);
    }

    private final List<Patient> items = new ArrayList<>();
    private final Listener listener;

    public PatientAdapter(Listener listener) {
        this.listener = listener;
    }

    public void setItems(List<Patient> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_patient, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        final Patient p = items.get(position);
        h.txtName.setText(p.getName());
        h.statusDot.setBackgroundResource(dotFor(p.getStatus()));

        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onOpenProfile(p); });
        h.btnReport.setOnClickListener(v -> { if (listener != null) listener.onOpenProfile(p); });
        h.btnUpload.setOnClickListener(v -> { if (listener != null) listener.onUpload(p); });
        h.btnFamily.setOnClickListener(v -> { if (listener != null) listener.onFamily(p); });
        h.btnDelete.setOnClickListener(v -> { if (listener != null) listener.onDelete(p); });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private int dotFor(int status) {
        switch (status) {
            case Patient.STATUS_RED:   return R.drawable.status_dot_red;
            case Patient.STATUS_GREEN: return R.drawable.status_dot_green;
            case Patient.STATUS_BLUE:  return R.drawable.status_dot_blue;
            case Patient.STATUS_GREY:
            default:                   return R.drawable.status_dot_grey;
        }
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView txtName;
        final View statusDot;
        final ImageView btnReport, btnUpload, btnFamily, btnDelete;

        VH(@NonNull View v) {
            super(v);
            txtName = v.findViewById(R.id.txtName);
            statusDot = v.findViewById(R.id.statusDot);
            btnReport = v.findViewById(R.id.btnReport);
            btnUpload = v.findViewById(R.id.btnUpload);
            btnFamily = v.findViewById(R.id.btnFamily);
            btnDelete = v.findViewById(R.id.btnDelete);
        }
    }
}
