package br.ifmg.edu.bsi.progmovel.shareimage1;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.DisplayMetrics;

/**
 * Cria um meme com um texto e uma imagem de fundo.
 *
 * Você pode controlar o texto, a cor do texto e a imagem de fundo.
 */
public class MemeCreator {
    private String texto;
    private String textoSuperior;
    private float textoX;
    private float textoSuperiorX;
    private float textoY;
    private float textoSuperiorY;
    private int corTexto;
    private int corTextoSuperior;
    private float tamTexto;
    private float tamTextoSuperior;
    private boolean isTextoSuperior; // escolher o texto a ser editado, false é o texto inferior e true o superior.
    private Bitmap fundo;
    private DisplayMetrics displayMetrics;
    private Bitmap meme;
    private boolean dirty; // se true, significa que o meme precisa ser recriado.

    public MemeCreator(String texto, int corTexto, float tamTexto, Bitmap fundo, DisplayMetrics displayMetrics) {
        this.texto = texto;
        this.corTexto = corTexto;
        this.tamTexto = tamTexto;
        this.textoX = -1;
        this.textoY = -1;
        this.textoSuperiorX = -1;
        this.textoSuperiorY = 1;
        isTextoSuperior = false;
        this.fundo = fundo;
        this.displayMetrics = displayMetrics;
        this.meme = criarImagem();
        this.dirty = false;
    }

    public String getTexto() {
        return texto;
    }

    public String getTextoSuperior() {
        return textoSuperior;
    }

    public void setTexto(String texto) {
        if (isTextoSuperior) {
            this.textoSuperior = texto;
        } else {
            this.texto = texto;
        }
        dirty = true;
    }

    public float getTextoX() {
        return textoX;
    }

    public float getTextoSuperiorX() {
        return textoSuperiorX;
    }

    public void setTextoX(float x) {
        if (isTextoSuperior) {
            this.textoSuperiorX = x;
        } else {
            this.textoX = x;
        }
        dirty = true;
    }

    public float getTextoY() {
        return textoY;
    }

    public float getTextoSuperiorY() {
        return textoSuperiorY;
    }
    public void setTextoY(float y) {
        if (isTextoSuperior) {
            this.textoSuperiorY = y;
        } else {
            this.textoY = y;
        }
        dirty = true;
    }

    public int getCorTexto() {
        return corTexto;
    }

    public void setCorTexto(int corTexto) {
        if (isTextoSuperior) {
            this.corTextoSuperior = corTexto;
        } else {
            this.corTexto = corTexto;
        }
        dirty = true;
    }

    public float getTamTexto() {
        return tamTexto;
    }

    public float getTamTextoSuperior() {
        return tamTextoSuperior;
    }

    public void setTamTexto(float tamTexto) {
        if (isTextoSuperior) {
            this.tamTextoSuperior = tamTexto;
        } else {
            this.tamTexto = tamTexto;
        }
        dirty = true;
    }

    public boolean isTextoSuperior() {
        return isTextoSuperior;
    }

    public void setIsTextoSuperior(boolean toggleTexto) {
        this.isTextoSuperior = toggleTexto;
        dirty = true;
    }

    public Bitmap getFundo() {
        return fundo;
    }

    public void setFundo(Bitmap fundo) {
        this.fundo = fundo;
        dirty = true;
    }

    public void rotacionarFundo(float graus) {
        Matrix matrix = new Matrix();
        matrix.postRotate(graus);
        fundo = Bitmap.createBitmap(fundo, 0, 0, fundo.getWidth(), fundo.getHeight(), matrix, true);
        dirty = true;
    }

    public Bitmap getImagem() {
        if (dirty) {
            meme = criarImagem();
            dirty = false;
        }
        return meme;
    }
    protected Bitmap criarImagem() {
        float heightFactor = (float) fundo.getHeight() / fundo.getWidth();
        int width = displayMetrics.widthPixels;
        int height = (int) (width * heightFactor);
        // nao deixa a imagem ocupar mais que 60% da altura da tela.
        if (height > displayMetrics.heightPixels * 0.6) {
            height = (int) (displayMetrics.heightPixels * 0.6);
            width = (int) (height * (1 / heightFactor));
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint paint = new Paint();

        Bitmap scaledFundo = Bitmap.createScaledBitmap(fundo, width, height, true);
        canvas.drawBitmap(scaledFundo, 0, 0, new Paint());

        paint.setColor(corTexto);
        paint.setAntiAlias(true);
        paint.setTextSize(getTamTexto());
        paint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
        paint.setTextAlign(Paint.Align.CENTER);
        // desenhar texto embaixo
        if (getTextoX() > -1 && getTextoY() > -1) {
            canvas.drawText(texto, getTextoX(), getTextoY(), paint);
        } else {
            canvas.drawText(texto, (width / 2.f), (height * 0.9f), paint);
        }
        if (getTextoSuperior() != null) {
            paint.setColor(corTexto);
            paint.setTextSize(getTamTextoSuperior());
            // desenhar texto em cima
            if (getTextoSuperiorX() > -1 && getTextoSuperiorY() > -1) {
                canvas.drawText(getTextoSuperior(), getTextoSuperiorX(), getTextoSuperiorY(), paint);
            } else {
                canvas.drawText(getTextoSuperior(), (width / 2.f), (height * 0.15f), paint);
            }
        }

        return bitmap;
    }
}
