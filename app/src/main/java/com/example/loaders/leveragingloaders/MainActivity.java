package com.example.loaders.leveragingloaders;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.loaders.leveragingloaders.models.User;
import com.example.loaders.leveragingloaders.services.GithubService;

import java.io.IOException;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity
    implements LoaderManager.LoaderCallbacks<List<User>> {

    private static final int LOADER_ID = 100;

    ProgressBar mUsersProgressBar;
    RecyclerView mUsersRecyclerView;
    GithubService mGithubService;
    UsersAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUsersRecyclerView = findViewById(R.id.recycler_view_users);
        mUsersProgressBar = findViewById(R.id.progress_bar_users);

        mAdapter = new UsersAdapter();
        mUsersRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mUsersRecyclerView.setAdapter(mAdapter);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mGithubService = retrofit.create(GithubService.class);

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            loadGithubUsers();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<User>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<User>>(this) {

            List<User> users;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();

                if (users == null) {
                    showLoadingIndicator();
                    forceLoad();
                } else {
                    deliverResult(users);
                }
            }

            @Override
            public List<User> loadInBackground() {
                try {
                    return mGithubService.getUsers().execute().body();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            public void deliverResult(List<User> data) {
                super.deliverResult(data);
                users = data;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<User>> loader, List<User> data) {
        if (data != null) {
            mAdapter.swapData(data);
        }

        showData();
    }

    @Override
    public void onLoaderReset(Loader<List<User>> loader) {
    }

    private void loadGithubUsers() {
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void showLoadingIndicator() {
        mUsersRecyclerView.setVisibility(View.INVISIBLE);
        mUsersProgressBar.setVisibility(View.VISIBLE);
    }

    private void showData() {
        mUsersRecyclerView.setVisibility(View.VISIBLE);
        mUsersProgressBar.setVisibility(View.INVISIBLE);
    }
}
