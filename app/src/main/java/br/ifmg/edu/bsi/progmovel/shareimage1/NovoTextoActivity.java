package br.ifmg.edu.bsi.progmovel.shareimage1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ToggleButton;

public class NovoTextoActivity extends AppCompatActivity {

    public static String EXTRA_TEXTO_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.texto_atual";
    public static String EXTRA_COR_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.cor_atual";
    public static String EXTRA_TAM_ATUAL = "br.ifmg.edu.bsi.progmovel.shareimage1.tam_atual";
    public static String EXTRA_NOVO_TEXTO = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_texto";
    public static String EXTRA_NOVA_COR = "br.ifmg.edu.bsi.progmovel.shareimage1.nova_cor";
    public static String EXTRA_NOVO_TAM = "br.ifmg.edu.bsi.progmovel.shareimage1.novo_tam";
    public static String EXTRA_TOGGLE_TEXTO = "br.ifmg.edu.bsi.progmovel.shareimage1.toggle_texto";

    private EditText etTexto;
    private EditText etCor;
    private EditText etTam;
    private ToggleButton toggleTexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novo_texto);

        etTexto = findViewById(R.id.etTexto);
        etCor = findViewById(R.id.etCor);
        etTam = findViewById(R.id.etTam);
        toggleTexto = findViewById(R.id.toggleTexto);

        Intent intent = getIntent();
        String textoAtual = intent.getStringExtra(EXTRA_TEXTO_ATUAL);
        String corAtual = intent.getStringExtra(EXTRA_COR_ATUAL);
        String tamAtual = intent.getStringExtra(EXTRA_TAM_ATUAL);
        etTexto.setText(textoAtual);
        etCor.setText(corAtual);
        etTam.setText(tamAtual);
    }

    public void enviarNovoTexto(View v) {
        String novoTexto = etTexto.getText().toString();
        String novaCor = etCor.getText().toString();
        String novoTam = etTam.getText().toString();
        boolean estadoToggle = toggleTexto.isChecked();
        Intent intent = new Intent();
        intent.putExtra(EXTRA_NOVO_TEXTO, novoTexto);
        intent.putExtra(EXTRA_NOVA_COR, novaCor);
        intent.putExtra(EXTRA_NOVO_TAM, novoTam);
        intent.putExtra(EXTRA_TOGGLE_TEXTO, estadoToggle);
        setResult(RESULT_OK, intent);
        finish();
    }
}