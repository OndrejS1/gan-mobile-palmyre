package cz.czu.palmyrenealphabettranscription;

import static java.lang.Math.min;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Trace;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import org.checkerframework.checker.units.qual.A;
import org.tensorflow.lite.support.image.ColorSpaceType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.vision.ImageProcessingOptions;
import org.tensorflow.lite.task.vision.classifier.Classifications;
import org.tensorflow.lite.task.vision.classifier.ImageClassifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {

    public static final String TAG = "ResultActivity";

    protected ImageClassifier imageClassifier;
    private static final int MAX_RESULTS = 3;
    private static final int NUM_THREADS = 1;

    TextView textView_bufferContent;
    Button button_clearBuffer;
    Button button_nextLetter;
    Button button_exportBuffer;

    ArrayList<String> bufferContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bufferContentString = sharedPreferences.getString("bufferContent", "");
        if(bufferContentString.equals(""))
        {
            bufferContent = new ArrayList<>();
        }
        else
        {
            String[] data = bufferContentString.split("\\|");
            bufferContent = new ArrayList<>(Arrays.asList(data));
        }

        textView_bufferContent = findViewById(R.id.textView_bufferContent);
        updateUIBuffer();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        ImageClassifier.ImageClassifierOptions options =
                ImageClassifier.ImageClassifierOptions.builder()
                        .setMaxResults(MAX_RESULTS)
                        .setNumThreads(NUM_THREADS)
                        .build();

        String inputType = getIntent().getStringExtra("InputType");
        //String modelPath = "tflitemodelall.tflite";
        String modelPath = "handwritten_model_v3_color_lite_meta.tflite";
        if(inputType.equals("photo"))
        {
            //modelPath = "tflitemodel_photo_all.tflite";
            modelPath = "photo_gan2_metadata.tflite";
            //modelPath = "model_v3_metadata.tflite";
        }

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(this, modelPath, options);
            Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bitmap inputBitmap = (Bitmap) getIntent().getParcelableExtra("BitmapForAnalysis");
        //inputBitmap = Bitmap.createScaledBitmap(inputBitmap, 224, 224, false);
        inputBitmap = Bitmap.createScaledBitmap(inputBitmap, 100, 100, false);
        //inputBitmap = toGrayscale(inputBitmap);

        ImageView imgView = (ImageView) findViewById(R.id.imageView);
        imgView.setImageBitmap(inputBitmap);
        //int width = inputBitmap.getWidth();
        //int height = inputBitmap.getHeight();

        List<Classifications> results = recognizeImage(inputBitmap);
        Log.d(TAG, results.toString());

        TableRow firstResult_row = (TableRow) findViewById(R.id.firstResult_row);
        TextView firstResult_symbol = (TextView) findViewById(R.id.firstResult_symbol);
        TextView firstResult_name = (TextView) findViewById(R.id.firstResult_name);
        TextView firstResult_transcription = (TextView) findViewById(R.id.firstResult_transcription);
        TextView firstResult_percentage = (TextView) findViewById(R.id.firstResult_percentage);

        TableRow secondResult_row = (TableRow) findViewById(R.id.secondResult_row);
        TextView secondResult_symbol = (TextView) findViewById(R.id.secondResult_symbol);
        TextView secondResult_name = (TextView) findViewById(R.id.secondResult_name);
        TextView secondResult_transcription = (TextView) findViewById(R.id.secondResult_transcription);
        TextView secondResult_percentage = (TextView) findViewById(R.id.secondResult_percentage);

        TableRow thirdResult_row = (TableRow) findViewById(R.id.thirdResult_row);
        TextView thirdResult_symbol = (TextView) findViewById(R.id.thirdResult_symbol);
        TextView thirdResult_name = (TextView) findViewById(R.id.thirdResult_name);
        TextView thirdResult_transcription = (TextView) findViewById(R.id.thirdResult_transcription);
        TextView thirdResult_percentage = (TextView) findViewById(R.id.thirdResult_percentage);

        List<Category> categories = results.get(0).getCategories();
        //String firstResult_label = categories.get(0).getLabel();
        //String firstResult_name_str = PalmyreneAlphabet.valueOf("aleph").getSymbol();

        PalmyreneAlphabet pa_firstResult = PalmyreneAlphabet.fromIndex(categories.get(0).getIndex());
        firstResult_symbol.setText(pa_firstResult.getSymbol());
        firstResult_name.setText(pa_firstResult.getName());
        firstResult_transcription.setText(pa_firstResult.getTranscription());
        firstResult_percentage.setText((Math.round(categories.get(0).getScore() * 10000)/100) + "");

        PalmyreneAlphabet pa_secondResult = PalmyreneAlphabet.fromIndex(categories.get(1).getIndex());
        secondResult_symbol.setText(pa_secondResult.getSymbol());
        secondResult_name.setText(pa_secondResult.getName());
        secondResult_transcription.setText(pa_secondResult.getTranscription());
        secondResult_percentage.setText((Math.round(categories.get(1).getScore() * 10000)/100) + "");

        PalmyreneAlphabet pa_thirdResult = PalmyreneAlphabet.fromIndex(categories.get(2).getIndex());
        thirdResult_symbol.setText(pa_thirdResult.getSymbol());
        thirdResult_name.setText(pa_thirdResult.getName());
        thirdResult_transcription.setText(pa_thirdResult.getTranscription());
        thirdResult_percentage.setText((Math.round(categories.get(2).getScore() * 10000)/100) + "");

        firstResult_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(results != null)
                {
                    bufferContent.add(pa_firstResult.getTranscription());
                    updateUIBuffer();
                    saveBuffer();
                }
            }
        });
        secondResult_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(results != null)
                {
                    bufferContent.add(pa_secondResult.getTranscription());
                    updateUIBuffer();
                    saveBuffer();
                }
            }
        });
        thirdResult_row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(results != null)
                {
                    bufferContent.add(pa_thirdResult.getTranscription());
                    updateUIBuffer();
                    saveBuffer();
                }
            }
        });

        button_nextLetter = findViewById(R.id.button_nextLetter);
        button_nextLetter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent previousActivityIntent = new Intent(getBaseContext(), DrawActivity.class);
                Log.d(TAG, "Going to scan next letter, input type: " + inputType);
                if (inputType.equals("photo"))
                {
                    Log.d(TAG, "Going to camera");
                    previousActivityIntent = new Intent(getBaseContext(), MainActivity.class);
                    previousActivityIntent.putExtra("OpenCamera", true);
                }
                startActivity(previousActivityIntent);
            }
        });

        button_clearBuffer = findViewById(R.id.button_clearBuffer);
        button_clearBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bufferContent.clear();
                updateUIBuffer();
                saveBuffer();
            }
        });

        button_exportBuffer = findViewById(R.id.button_exportBuffer);
        button_exportBuffer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                String bufferStringContent = (String) TextUtils.join(" | ", bufferContent);
                sendIntent.putExtra(Intent.EXTRA_TEXT, bufferStringContent);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<Classifications> recognizeImage(final Bitmap bitmap) {
        // Logs this method so that it can be analyzed with systrace.
        Trace.beginSection("recognizeImage");

        TensorImage inputImage = TensorImage.fromBitmap(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int cropSize = min(width, height);
        // TODO(b/169379396): investigate the impact of the resize algorithm on accuracy.
        // Task Library resize the images using bilinear interpolation, which is slightly different from
        // the nearest neighbor sampling algorithm used in lib_support. See
        // https://github.com/tensorflow/examples/blob/0ef3d93e2af95d325c70ef3bcbbd6844d0631e07/lite/examples/image_classification/android/lib_support/src/main/java/org/tensorflow/lite/examples/classification/tflite/Classifier.java#L310.
        ImageProcessingOptions imageOptions =
                ImageProcessingOptions.builder()
                        // Set the ROI to the center of the image.
                        .setRoi(
                                new Rect(
                                        /*left=*/ (width - cropSize) / 2,
                                        /*top=*/ (height - cropSize) / 2,
                                        /*right=*/ (width + cropSize) / 2,
                                        /*bottom=*/ (height + cropSize) / 2))
                        .build();

        // Runs the inference call.
        Trace.beginSection("runInference");
        long startTimeForReference = SystemClock.uptimeMillis();
        List<Classifications> results = imageClassifier.classify(inputImage, imageOptions);
        long endTimeForReference = SystemClock.uptimeMillis();
        Trace.endSection();
        Log.v(TAG, "Timecost to run model inference: " + (endTimeForReference - startTimeForReference));

        Trace.endSection();

        return results;
    }
    private void saveBuffer() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor spEditor = sharedPreferences.edit();
        spEditor.clear(); //surprisingly very important...
        spEditor.putString("bufferContent", (String) TextUtils.join("|", bufferContent));
        spEditor.commit();
    }
    private void updateUIBuffer() {
        textView_bufferContent.setText((String) TextUtils.join(" | ", bufferContent));
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal)
    {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }
}