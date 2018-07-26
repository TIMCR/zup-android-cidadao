package br.com.lfdb.particity.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.com.lfdb.particity.R;
import br.com.lfdb.particity.SoliciteActivity;
import br.com.lfdb.particity.base.BaseFragment;
import br.com.lfdb.particity.domain.Solicitacao;
import br.com.lfdb.particity.service.FeatureService;
import br.com.lfdb.particity.util.FileUtils;
import br.com.lfdb.particity.util.FontUtils;
import br.com.lfdb.particity.util.ImageUtils;
import eu.janmuller.android.simplecropimage.CropImage;

public class SoliciteFotosFragment extends BaseFragment implements View.OnClickListener {

    private TextView fotoButton;
    private static final int CAMERA_RETURN = 1406;
    private static final int CROP_RETURN = 1407;
    private static final int GALLERY_RETURN = 1408;
    private Uri imagemTemporaria;
    private ImageView fotoFrame;
    private LinearLayout containerFotos;
    private List<String> listaFotos = new ArrayList<>();

    private View temp = null;

    private static final int MARSHMALLOW = 23;
    private String path_provider = "br.com.lfdb.particity.provider";
    private String mCurrentPhotoPath;

    @Override public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            ((SoliciteActivity) getActivity()).setInfo(R.string.adicione_fotos);
        }
    }

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPermission();
        setRetainInstance(true);
    }

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                       Bundle savedInstanceState) {
        ((SoliciteActivity) getActivity()).setInfo(R.string.adicione_fotos);

        View view = inflater.inflate(R.layout.fragment_solicite_fotos, container, false);

        fotoFrame = (ImageView) view.findViewById(R.id.fotoFrame);
        containerFotos = (LinearLayout) view.findViewById(R.id.containerFotos);

        fotoButton = (TextView) view.findViewById(R.id.fotoButton);
        fotoButton.setOnClickListener(this);
        fotoButton.setTypeface(FontUtils.getRegular(getActivity()));

        return view;
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey("imagemTemporaria")) {
            imagemTemporaria = Uri.parse(getArguments().getString("imagemTemporaria"));
        }

        ArrayList<String> fotos = null;
        if (getArguments() != null) {
            Solicitacao solicitacao = (Solicitacao) getArguments().getSerializable("solicitacao");
            if (solicitacao != null) {
                fotos = solicitacao.getFotos();
            }
        }

        if (fotos == null || fotos.isEmpty()) {
            fotos = ((SoliciteActivity) getActivity()).getFotos();
        }

        if (fotos != null) {
            for (String foto : fotos) {
                adicionarFoto(foto);
            }
        }
    }

    @Override public void onClick(View v) {
        if (listaFotos != null && listaFotos.size() == 3) {
            new AlertDialog.Builder(getActivity()).setMessage(getString(R.string.only_three_photos))
                    .setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                    .show();
            return;
        }

        if (FeatureService.getInstance(getActivity()).isAllowPhotoAlbumAccessEnabled()) {
            new AlertDialog.Builder(getActivity()).setItems(R.array.foto_menu, (dialog, item) -> {
                switch (item) {
                    case 0:
                        selecionarFoto();
                        break;
                    case 1:
                        tirarFoto();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                }
            }).show();
        } else {
            new AlertDialog.Builder(getActivity()).setItems(R.array.foto_menu_restricted,
                    (dialog, item) -> {
                        switch (item) {
                            case 0:
                                tirarFoto();
                                break;
                            case 1:
                                dialog.dismiss();
                                break;
                        }
                    }).show();
        }
    }

    private void selecionarFoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GALLERY_RETURN);
    }

    private void tirarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Arquivo temporário
            if (Build.VERSION.SDK_INT < MARSHMALLOW) {
                imagemTemporaria = Uri.fromFile(new File(FileUtils.getTempImagesFolder(),
                        "tmp_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));
            } else {
                imagemTemporaria = FileProvider.getUriForFile(getActivity(),
                        path_provider, createImageFile());
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemTemporaria);
            intent.putExtra("return-data", true);
            startActivityForResult(intent, CAMERA_RETURN);
        }
    }

    private void setPermission() {
        int permission = ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1
            );
        }
    }

    private File createImageFile() {
        String imageFileName = "tmp_image_" + String.valueOf(System.currentTimeMillis());
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = null;
        try {
            image = File.createTempFile(
                    imageFileName,
                    ".jpg",
                    storageDir
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            temp = null;
            return;
        }
        switch (requestCode) {
            case CROP_RETURN:
                String path = data.getStringExtra(CropImage.IMAGE_PATH);
                if (path == null) {
                    temp = null;
                    return;
                }
                removerFoto(temp);
                ((SoliciteActivity) getActivity()).adicionarFoto(path);
                ((SoliciteActivity) getActivity()).assertFragmentVisibility();
                adicionarFoto(path);
                break;
            case GALLERY_RETURN:
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                if (selectedImage == null) return;
                Cursor cursor = getActivity().getContentResolver()
                        .query(selectedImage, filePathColumn, null, null, null);
                if (cursor == null) return;
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                if (Build.VERSION.SDK_INT < MARSHMALLOW) {

                    imagemTemporaria = Uri.fromFile(new File(picturePath));

                } else {
                    File destiny = createImageFile();
                    File source =  new File(picturePath);

                    try {
                        Files.copy(source, destiny);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    mCurrentPhotoPath = destiny.getAbsolutePath();
                }

            case CAMERA_RETURN:
                Intent intent = new Intent(getActivity(), CropImage.class);

                if (Build.VERSION.SDK_INT < MARSHMALLOW) {
                    intent.putExtra(CropImage.IMAGE_PATH, imagemTemporaria.getPath());

                    imagemTemporaria = Uri.fromFile(
                            new File(FileUtils.getTempImagesFolder(), System.currentTimeMillis() + ".jpg"));
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemTemporaria);
                } else {
                    intent.putExtra(CropImage.IMAGE_PATH, mCurrentPhotoPath);

                    imagemTemporaria = FileProvider.getUriForFile(getActivity(),
                            path_provider, createImageFile());

                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imagemTemporaria);
                }

                intent.putExtra(CropImage.SCALE, true);
                intent.putExtra(CropImage.ASPECT_X, 1);
                intent.putExtra(CropImage.ASPECT_Y, 1);
                intent.putExtra(CropImage.OUTPUT_X, 800);
                intent.putExtra(CropImage.OUTPUT_Y, 800);

                startActivityForResult(intent, CROP_RETURN);
                break;
        }
    }

    public String getImagemTemporaria() {
        return imagemTemporaria != null ? imagemTemporaria.getPath() : "";
    }

    private void removerFoto(View view) {
        if (view == null) return;

        String foto = (String) view.getTag();
        containerFotos.removeView(view);
        ((SoliciteActivity) getActivity()).removerFoto(foto);
        listaFotos.remove(foto);

        if (listaFotos.isEmpty()) {
            fotoFrame.setVisibility(View.VISIBLE);
            containerFotos.setVisibility(View.GONE);
        } else {
            containerFotos.setWeightSum(listaFotos.size());
        }
    }

    private void adicionarFoto(String foto) {
        Bitmap bitmap = BitmapFactory.decodeFile(foto);
        listaFotos.add(foto);

        fotoFrame.setVisibility(View.GONE);

        RelativeLayout layout = new RelativeLayout(getActivity());

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        if (listaFotos.size() > 1) {
            lp.setMargins((int) ImageUtils.dpToPx(getActivity(), 5), 0, 0, 0);
        }
        layout.setLayoutParams(lp);

        ImageView imgView = new ImageView(getActivity());
        imgView.setId((int) System.currentTimeMillis());

        if (getResources().getDimension(R.dimen.image_resize) != 0) {
            imgView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
                    (int) (getResources().getDimension(R.dimen.image_resize)
                            / getResources().getDisplayMetrics().density),
                    (int) (getResources().getDimension(R.dimen.image_resize)
                            / getResources().getDisplayMetrics().density), false));
        } else {
            imgView.setImageBitmap(bitmap);
        }
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);

        imgView.setLayoutParams(layoutParams);

        layout.addView(imgView);

        ImageView btn = new ImageView(getActivity());
        btn.setClickable(true);
        btn.setImageResource(R.drawable.btn_editar_foto);
        btn.setOnClickListener(
                v -> new AlertDialog.Builder(getActivity()).setItems(R.array.foto_menu_editar,
                        (dialog, item) -> {
                            switch (item) {
                                case 0:
                                    removerFoto((View) v.getParent());
                                    fotoButton.setEnabled(true);
                                    break;
                                case 1:
                                    temp = (View) v.getParent();
                                    selecionarFoto();
                                    break;
                                case 2:
                                    temp = (View) v.getParent();
                                    tirarFoto();
                                    break;
                                case 3:
                                    dialog.dismiss();
                                    break;
                            }
                        }).show());
        RelativeLayout.LayoutParams params =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_TOP, imgView.getId());
        params.addRule(RelativeLayout.ALIGN_RIGHT, imgView.getId());
        btn.setLayoutParams(params);

        layout.addView(btn);
        layout.setTag(foto);

        containerFotos.setVisibility(View.VISIBLE);
        containerFotos.setWeightSum(listaFotos.size());
        containerFotos.addView(layout);
    }

    @Override protected String getScreenName() {
        return "Adição de Fotos (Novo Relato)";
    }
}
