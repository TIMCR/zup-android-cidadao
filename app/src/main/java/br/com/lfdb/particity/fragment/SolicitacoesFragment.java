package br.com.lfdb.particity.fragment;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SolicitacaoDetalheActivity;
import br.com.lfdb.particity.domain.SolicitacaoListItem;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;

public class SolicitacoesFragment extends ListFragment implements AdapterView.OnItemClickListener {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_solicitacoes, container, false);
	}

	public class SolicitacaoAdapter extends ArrayAdapter<SolicitacaoListItem> {

		private List<SolicitacaoListItem> items;

		public SolicitacaoAdapter(Context context, List<SolicitacaoListItem> objects) {
			super(context, R.layout.solicitacao_row, objects);
			items = objects;
		}

		@SuppressLint("NewApi")
		@SuppressWarnings("deprecation")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row = getActivity().getLayoutInflater().inflate(R.layout.solicitacao_row, parent, false);
			SolicitacaoListItem item = items.get(position);

			TextView titulo = (TextView) row.findViewById(R.id.titulo);
			titulo.setText(item.getTitulo());
			titulo.setTypeface(FontUtils.getLight(getContext()));
			
			TextView data = (TextView) row.findViewById(R.id.data);
			data.setText(item.getData());
			data.setTypeface(FontUtils.getBold(getContext()));
			
			TextView protocolo = (TextView) row.findViewById(R.id.protocolo);
			protocolo.setText(getString(R.string.protocolo) + " " + item.getProtocolo());
			protocolo.setTypeface(FontUtils.getRegular(getContext()));
			
			row.findViewById(R.id.bg).setBackgroundColor(item.getStatus().getCor());
			TextView indicadorStatus = (TextView) row.findViewById(R.id.indicadorStatus);
			indicadorStatus.setTypeface(FontUtils.getBold(getContext()));
			int fiveDp = (int) ImageUtils.dpToPx(getActivity(), 5);
			int tenDp = (int) ImageUtils.dpToPx(getActivity(), 10);
			indicadorStatus.setPadding(tenDp, fiveDp, tenDp, fiveDp);
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
				indicadorStatus.setBackgroundDrawable(ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
			} else {
				indicadorStatus.setBackground(ImageUtils.getStatusBackground(getActivity(), item.getStatus().getCor()));
			}
			indicadorStatus.setText(item.getStatus().getNome());

			return row;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SolicitacaoListItem item = (SolicitacaoListItem) parent.getItemAtPosition(position);
		Intent intent = new Intent(getActivity(), SolicitacaoDetalheActivity.class);
		intent.putExtra("solicitacao", item);
		intent.putExtra("alterar_botao", true);
		startActivity(intent);
	}
	
	public void setRelatos(List<SolicitacaoListItem> relatos) {
		getListView().setAdapter(new SolicitacaoAdapter(getActivity(), relatos));
		getListView().setOnItemClickListener(this);
	}
}
