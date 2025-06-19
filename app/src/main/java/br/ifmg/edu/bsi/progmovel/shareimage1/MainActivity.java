package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia;
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly;
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import java.io.BufferedOutputStream;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Activity que cria uma imagem com um texto e imagem de fundo.
 */
public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private MemeCreator memeCreator;
    int templateAtual = 0;
    private int[] templates = {R.drawable.fry_meme, R.drawable.aliens_meme, R.drawable.butterfly_meme, R.drawable.buzz_meme, R.drawable.antonio_meme};
    private boolean allowMovement;
    private final ActivityResultLauncher<Intent> startNovoTexto = registerForActivityResult(new StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent intent = result.getData();
                        if (intent != null) {
                            String novoTexto = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TEXTO);
                            String novaCor = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVA_COR);
                            String novoTam = intent.getStringExtra(NovoTextoActivity.EXTRA_NOVO_TAM);
                            boolean toggleTexto = intent.getBooleanExtra(NovoTextoActivity.EXTRA_TOGGLE_TEXTO, false);
                            if (novaCor == null || novaCor.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Cor desconhecida. Usando preto no lugar.", Toast.LENGTH_SHORT).show();
                                novaCor = "BLACK";
                            }
                            if (novoTam == null || novoTam.isEmpty()) {
                                Toast.makeText(MainActivity.this, "Tamanho desconhecido. Usando 64 no lugar.", Toast.LENGTH_SHORT).show();
                                novoTam = "64";
                            }
                            memeCreator.setIsTextoSuperior(toggleTexto);
                            memeCreator.setTexto(novoTexto);
                            memeCreator.setCorTexto(Color.parseColor(novaCor.toUpperCase()));
                            memeCreator.setTamTexto(Float.parseFloat(novoTam));
                            mostrarImagem();
                        }
                    }
                }
            });

    private final ActivityResultLauncher<PickVisualMediaRequest> startImagemFundo = registerForActivityResult(new PickVisualMedia(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    if (result == null) {
                        return;
                    }
                    try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(result, "r")) {
                        Bitmap imagemFundo = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), result);
                        memeCreator.setFundo(imagemFundo);

                        // descobrir se é preciso rotacionar a imagem
                        FileDescriptor fd = pfd.getFileDescriptor();
                        ExifInterface exif = new ExifInterface(fd);
                        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                        if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                            memeCreator.rotacionarFundo(90);
                        }

                        mostrarImagem();
                    } catch (IOException e) {
                        Toast.makeText(MainActivity.this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            });

    private ActivityResultLauncher<String> startWriteStoragePermission = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if (!result) {
                        Toast.makeText(MainActivity.this, "Sem permissão de acesso a armazenamento do celular.", Toast.LENGTH_SHORT).show();
                    } else {
                        compartilhar(null);
                    }
                }
            });

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = findViewById(R.id.imageView);
        float[] ultimoToqueXY = new float[2];
        allowMovement = false;

        Bitmap imagemFundo = BitmapFactory.decodeResource(getResources(), templates[templateAtual]);

        memeCreator = new MemeCreator("Olá Android!", Color.WHITE, 64.f, imagemFundo, getResources().getDisplayMetrics());
        mostrarImagem();

        imageView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                ultimoToqueXY[0] = event.getX();
                ultimoToqueXY[1] = event.getY();
            }
            return false;
        });

        imageView.setOnLongClickListener(e -> {
            Toast.makeText(getApplicationContext(), "long click", Toast.LENGTH_LONG).show();
            Log.d("MEMECREATOR", "Long pressed");
            allowMovement = true;
            return true;
        });

        imageView.setOnClickListener((v) -> {
            if (allowMovement) {
                int eixoLargura = imageView.getWidth();
                int eixoAltura = imageView.getHeight();
                float x = ultimoToqueXY[0];
                float y = ultimoToqueXY[1];
                memeCreator.setTextoX(x);
                memeCreator.setTextoY(y);
                mostrarImagem();
                allowMovement = false;
            }
        });
    }

    public void iniciarMudarTexto(View v) {
        Intent intent = new Intent(this, NovoTextoActivity.class);
        intent.putExtra(NovoTextoActivity.EXTRA_TEXTO_ATUAL, memeCreator.getTexto());
        intent.putExtra(NovoTextoActivity.EXTRA_COR_ATUAL, converterCor(memeCreator.getCorTexto()));

        startNovoTexto.launch(intent);
    }

    public String converterCor(int cor) {
        switch (cor) {
            case Color.BLACK: return "BLACK";
            case Color.WHITE: return "WHITE";
            case Color.BLUE: return "BLUE";
            case Color.GREEN: return "GREEN";
            case Color.RED: return "RED";
            case Color.YELLOW: return "YELLOW";
        }
        return null;
    }

    public void iniciarMudarFundo(View v) {
        startImagemFundo.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ImageOnly.INSTANCE)
                .build());
    }

    public void mudarTemplate(View v) {
        if (templateAtual >= templates.length - 1) {
            templateAtual = 0;
        } else {
            templateAtual++;
        }
        Bitmap background = BitmapFactory.decodeResource(getResources(), templates[templateAtual]);
        memeCreator.setFundo(background);
        mostrarImagem();
    }

    public void compartilhar(View v) {
        compartilharImagem(memeCreator.getImagem());
    }

    public void mostrarImagem() {
        imageView.setImageBitmap(memeCreator.getImagem());
    }

    public void compartilharImagem(Bitmap bitmap) {

        // pegar a uri da mediastore
        // pego o volume externo pq normalmente ele é maior que o volume interno.
        Uri contentUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        } else {
            /*
            Em versões <= 28, é preciso solicitar a permissão WRITE_EXTERNAL_STORAGE.
            Mais detalhes em https://developer.android.com/training/data-storage/shared/media#java.
             */
            int write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (PackageManager.PERMISSION_GRANTED != write) {
                startWriteStoragePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return;
            }
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        // montar a nova imagem a ser inserida na mediastore
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, "shareimage1file");
        values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        Uri imageUri = getContentResolver().insert(contentUri, values);

        // criar a nova imagem na pasta da mediastore
        try (
                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(imageUri, "w");
                FileOutputStream fos = new FileOutputStream(pfd.getFileDescriptor())
            ) {
            BufferedOutputStream bytes = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao gravar imagem:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        // compartilhar a imagem com intent implícito
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/*");
        share.putExtra(Intent.EXTRA_TITLE, "Seu meme fabuloso");
        share.putExtra(Intent.EXTRA_STREAM, imageUri);
        startActivity(Intent.createChooser(share, "Compartilhar Imagem"));
    }
}