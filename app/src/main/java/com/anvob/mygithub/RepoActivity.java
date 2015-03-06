package com.anvob.mygithub;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.RepositoryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class RepoActivity extends ActionBarActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private ListView listView;
    private View mProgressView;
    private User user;
    private RepoLoadTask mAuthTask = null;
    private List<Repository> repos;
    private RepoArrAdapter listAdapter;
    private Bundle bun;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repo);
        listView = (ListView)findViewById(R.id.repo_listView);
        mProgressView = findViewById(R.id.repo_progress);
        if(savedInstanceState==null) {
            repos = new ArrayList<Repository>();
            user = (User) getIntent().getSerializableExtra(StringConstants.USER);
        }
        else
        {
            repos = (ArrayList<Repository>)savedInstanceState.getSerializable(StringConstants.REPOS_LIST);
            user = (User)savedInstanceState.getSerializable(StringConstants.USER);
        }
        //Repository
        listAdapter = new RepoArrAdapter(this, repos);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        if(savedInstanceState==null) {
            loadTask(user,this);
        }

    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        mAuthTask = null;
        showProgress(false);
    }
    @Override
    protected  void onPause()
    {
        super.onPause();
        mAuthTask = null;
        showProgress(false);
    }
    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(StringConstants.REPOS_LIST, (java.io.Serializable) repos);
        outState.putSerializable(StringConstants.USER,user);
    }
    private void loadTask(User user, Activity activity){
        if (user != null) {
            repos.clear();
            showProgress(true);
            mAuthTask = new RepoLoadTask(this, user);
            mAuthTask.execute((Void) null);
        } else {
            //Toast.makeText(this, R.string.error_load_user, Toast.LENGTH_SHORT);
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
            //mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0)finish();
    }
    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.question_exit);
        builder.setCancelable(true);
        builder.setPositiveButton(R.string.action_exit, new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which) {
                setResult(0);
                finish();
            }
        });
        builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // выводим диалоговое окно
        builder.show();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_exit) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setMessage(R.string.question_exit);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.action_exit, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    setResult(0);
                    finish();
                }
            });
            builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // выводим диалоговое окно
            builder.show();

            return true;
        }
        if (id == R.id.action_logout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.warning);
            builder.setMessage(R.string.question_logout);
            builder.setCancelable(true);
            builder.setPositiveButton(R.string.action_logout, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(getBaseContext(),LoginActivity.class);
                    i.putExtra(StringConstants.LOGOUT,true);
                    startActivityForResult(i,0);
                    //finish();
                }
            });
            builder.setNegativeButton(R.string.action_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            // выводим диалоговое окно
            builder.show();

            return true;
        }
        if (id == R.id.action_update) {
            loadTask(user,this);
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onClick(View v) {

    }
    public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id){
        showActivity(repos.get(position));
    }
    public class RepoLoadTask extends AsyncTask<Void, Void, Boolean> {

        String str="";
        User user;
        Activity context;
        RepoLoadTask(Activity context, User u) {
            this.user=u;
            this.context=context;
        }

        @Override
        protected Boolean doInBackground(Void... params) {


            RepositoryService service = new RepositoryService();
            try {
                for (Repository repo : service.getRepositories(user.getLogin())) {
                    System.out.println(repo.getName() + " Watchers: " + repo.getWatchers());
                    repos.add(repo);
                }
                System.out.println("repos size = "+ repos.size());

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

            if (success){

            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.error_repos);
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
            //    mPasswordView.setError(getString(R.string.error_incorrect_password));
            //    mPasswordView.requestFocus();
            //}/
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    private void showActivity(Repository repo) {
        Intent i = new Intent(this, CommitActivity.class);
        i.putExtra(StringConstants.REPOSITORIES,repo);
        startActivity(i);
    }
}
