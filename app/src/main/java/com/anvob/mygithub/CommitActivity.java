package com.anvob.mygithub;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.service.CommitService;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


public class CommitActivity extends ActionBarActivity {

    private View mProgressView;
    private CommitLoadTask mAuthTask = null;
    private ListView listView;
    private ImageView userImage;
    private TextView tv_user_name;
    private TextView tv_repo_name;
    private TextView tv_repo_desc;
    private TextView tv_repo_forks;
    private TextView tv_repo_watchers;
    private Repository repo;
    private List<RepositoryCommit> commits;
    private CommitArrayAdapter listAdapter;
    private ProgressDialog progressDialog;
    private DownloadTask downloadTask;
    //private
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit);
        listView = (ListView)findViewById(R.id.commit_ListView);
        mProgressView = findViewById(R.id.commit_progress);
        tv_user_name = (TextView)findViewById(R.id.tv_commit_own_name);
        tv_repo_name = (TextView)findViewById(R.id.tv_commit_repo_name);
        tv_repo_desc = (TextView)findViewById(R.id.tv_commit_repo_desc);
        tv_repo_forks = (TextView)findViewById(R.id.tv_commit_repo_forks);
        tv_repo_watchers = (TextView)findViewById(R.id.tv_commit_repo_watchers);
        userImage = (ImageView)findViewById(R.id.iv_commit_own_img);
        if(savedInstanceState==null) {
            commits = new ArrayList<RepositoryCommit>();
            repo = (Repository)getIntent().getSerializableExtra(StringConstants.REPOSITORIES);
        }
        else
        {
            commits = (ArrayList<RepositoryCommit>)savedInstanceState.getSerializable(StringConstants.COMMITS_LIST);
            if(commits==null)commits = new ArrayList<RepositoryCommit>();
            repo = (Repository)savedInstanceState.getSerializable(StringConstants.REPOSITORIES);
            if(repo!=null)showRepoInfo(repo);
            Bitmap bitmap = (Bitmap)savedInstanceState.getParcelable(StringConstants.USER_IMAGE);
            if(bitmap!=null)userImage.setImageBitmap(bitmap);

        }
        listAdapter = new CommitArrayAdapter(this, commits);
        listView.setAdapter(listAdapter);
        if(savedInstanceState==null) {
            loadTask(repo,this);
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mAuthTask = null;
        downloadTask=null;
        showProgress(false);
    }
    @Override
    protected  void onPause()
    {
        super.onPause();
        mAuthTask = null;
        downloadTask=null;
        showProgress(false);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(StringConstants.COMMITS_LIST, (java.io.Serializable) commits);
        outState.putSerializable(StringConstants.REPOSITORIES,repo);
        Drawable d = userImage.getDrawable();
        outState.putParcelable(StringConstants.USER_IMAGE, drawableToBitmap(d));
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_commits, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_update_comm) {
            loadTask(repo,this);
        }
        return super.onOptionsItemSelected(item);
    }
    private void loadTask(Repository repo, Activity activity)
    {
        if (repo != null) {
            showRepoInfo(repo);
            commits.clear();
            if (NetworkManager.isNetworkAvailable(this)) {
                showProgress(true);
                downloadTask = new DownloadTask(this);
                //Запускаем задачу, передавая ей ссылку на картинку
                downloadTask.execute(repo.getOwner().getAvatarUrl());
                mAuthTask = new CommitLoadTask(this, repo);
                mAuthTask.execute((Void) null);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.error_internet);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // выводим диалоговое окно
                builder.show();
            }
        } else {
            //Toast.makeText(this, R.string.error_load_repo, Toast.LENGTH_SHORT);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setMessage(R.string.error_load_repo);
            builder.setCancelable(true);
            builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // выводим диалоговое окно
            builder.show();
        }
    }
    private boolean showRepoInfo(Repository repo)
    {
        tv_repo_name.setText(repo.getName());
        tv_repo_desc.setText(repo.getDescription());
        tv_repo_forks.setText("Forks: "+repo.getForks());
        tv_repo_watchers.setText("Watchers: "+repo.getWatchers());
        tv_user_name.setText(repo.getOwner().getLogin());
        //userImage.animate().setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        return true;
    }


    class DownloadTask extends AsyncTask<String, Integer, Bitmap>{

        Drawable draw;
        Activity context;
        @Override
        protected void onPreExecute() {
            //Отображаем системный диалог загрузки
           /* progressDialog = new ProgressDialog(CommitActivity.this);
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgress(0);
            progressDialog.setProgressStyle(ProgressDialog.THEME_TRADITIONAL);
            progressDialog.setMessage("Downloading Image");
            progressDialog.show();*/
        }
        DownloadTask(Activity context) {
            this.context=context;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            //В этом методе происходит загрузка картинки через
            //стандартный класс URLConnection
            int count;
            BufferedInputStream buf_stream = null;
            Bitmap bitmap = null;
            try {
                URL url = new URL(params[0]);
                URLConnection conn = url.openConnection();
                conn.setDoInput(true);
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.connect();
                buf_stream = new BufferedInputStream(conn.getInputStream(), 8192);
                bitmap = BitmapFactory. decodeStream(buf_stream);
                buf_stream.close();

            } catch (Exception e) {
                //Log.e("Error: ", e.getMessage());
            }

            return bitmap;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            //progressDialog.setProgress(progress[0]);
        }

        //Скроем диалог и покажем картинку
        @Override
        protected void onPostExecute(Bitmap result) {
            userImage.clearAnimation();
            if(result!=null) {
                userImage.setImageBitmap(result);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.error_ava);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // выводим диалоговое окно
                builder.show();
            }

            //progressDialog.dismiss();
        }

        //Этот метод будет вызван вместо onPostExecute,
        //если мы остановили выполнение задачи методом
        //AsyncTask#cancel(boolean mayInterruptIfRunning)
        @Override
        protected void onCancelled() {
            downloadTask=null;
        }
    }

    public class CommitLoadTask extends AsyncTask<Void, Void, Boolean> {

        String str="";
        Repository repo;
        Activity context;
        CommitLoadTask(Activity context, Repository repo) {
            this.repo=repo;
            this.context=context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            CommitService service = new CommitService();
            try {
                for (RepositoryCommit comm : service.getCommits(repo)) {
                    //System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
                    commits.add(comm);
                }
                System.out.println("repos size = "+ commits.size());

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);
            if (success) {
                System.out.println("repos size = " + commits.size());
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.error_commits);
                builder.setCancelable(true);
                builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                // выводим диалоговое окно
                builder.show();
            }
            listAdapter.notifyDataSetChanged();

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    /**
     * Shows the progress UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        // We ask for the bounds if they have been set as they would be most
        // correct, then we check we are  > 0
        final int width = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().width() : drawable.getIntrinsicWidth();

        final int height = !drawable.getBounds().isEmpty() ?
                drawable.getBounds().height() : drawable.getIntrinsicHeight();

        // Now we check we are > 0
        final Bitmap bitmap = Bitmap.createBitmap(width <= 0 ? 1 : width, height <= 0 ? 1 : height,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
