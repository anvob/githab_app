package com.anvob.mygithub;



import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.eclipse.egit.github.core.Repository;
import java.util.List;

/**
 * Created by anvob on 02.03.2015.
 * ArrayAdapter for repository listview
 */
public class RepoArrAdapter  extends ArrayAdapter<Repository> //implements OnCheckedChangeListener
{
    private final Activity context;
    private final List<Repository> list;
    public RepoArrAdapter(Activity context, List<Repository> list)
    {
        super(context, R.layout.list_repo_item, list);
        this.context = context;
        this.list=list;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflator = context.getLayoutInflater();
        view = inflator.inflate(R.layout.list_repo_item,null);
        TextView tv_name = (TextView) view.findViewById(R.id.tv_repo_item_name);
        TextView tv_desc = (TextView) view.findViewById(R.id.tv_repo_item_desc);
        TextView tv_forks = (TextView) view.findViewById(R.id.tv_repo_item_forks);
        TextView tv_watchers = (TextView) view.findViewById(R.id.tv_repo_item_watchers);
        TextView tv_lang = (TextView) view.findViewById(R.id.tv_repo_item_lang);

        tv_name.setText(list.get(position).getName());
        tv_desc.setText(list.get(position).getDescription());
        tv_lang.setText(list.get(position).getLanguage());
        tv_forks.setText("Forks "+list.get(position).getForks());
        tv_watchers.setText("Wathers "+list.get(position).getWatchers());

        return view;
    }
}

