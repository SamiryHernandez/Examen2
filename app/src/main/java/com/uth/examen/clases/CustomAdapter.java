package com.uth.examen.clases;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;

import com.uth.examen.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private List<Entrevistas> entrevistasList;
    private Context context;

    public CustomAdapter(Context context, List<Entrevistas> entrevistasList) {
        this.context = context;
        this.entrevistasList = entrevistasList;
    }

    @Override
    public int getCount() {
        return entrevistasList.size();
    }

    @Override
    public Object getItem(int position) {
        return entrevistasList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Entrevistas entrevista = entrevistasList.get(position);

        viewHolder.getTxtPeriodista().setText(entrevista.getPeriodista());
        viewHolder.getTxtDescripcion().setText(entrevista.getDescripcion());

        if (entrevista.getImagen() != null && !entrevista.getImagen().isEmpty()) {
            Bitmap bitmap = decodeBase64(entrevista.getImagen());
            viewHolder.getImgView().setImageBitmap(bitmap);
        }

        return convertView;
    }

    static class ViewHolder {
        private final TextView txtPeriodista;
        private final TextView txtDescripcion;
        private final ImageView imgView;


        public ViewHolder(@NonNull View view) {
            txtPeriodista = view.findViewById(R.id.txtLPeriodista);
            txtDescripcion = view.findViewById(R.id.txtLDescripcion);
            imgView = view.findViewById(R.id.txtLImage);
        }

        public TextView getTxtPeriodista() {
            return txtPeriodista;
        }

        public TextView getTxtDescripcion() {
            return txtDescripcion;
        }

        public ImageView getImgView() {
            return imgView;
        }
    }

    private Bitmap decodeBase64(String base64) {
        byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
}

