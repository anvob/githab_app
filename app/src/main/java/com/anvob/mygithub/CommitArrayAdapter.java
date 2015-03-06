package com.anvob.mygithub;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.RepositoryCommit;

import java.util.Calendar;
import java.util.Formatter;
import java.util.List;

/**
 * Created by anvob on 02.03.2015.
 */
public class CommitArrayAdapter extends ArrayAdapter<RepositoryCommit> {
    private final Activity context;
    private final List<RepositoryCommit> list;
    public CommitArrayAdapter(Activity context, List<RepositoryCommit> list)
    {
        super(context, R.layout.list_repo_item, list);
        this.context = context;
        this.list=list;
    }
    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        LayoutInflater inflator = context.getLayoutInflater();
        view = inflator.inflate(R.layout.list_commit_item,null);
        TextView tv_msg = (TextView) view.findViewById(R.id.tv_commit_item_msg);
        TextView tv_hash = (TextView) view.findViewById(R.id.tv_commit_item_hash);
        TextView tv_date = (TextView) view.findViewById(R.id.tv_commit_item_date);
        TextView tv_owner = (TextView) view.findViewById(R.id.tv_commit_item_owner);

        Commit comm = list.get(position).getCommit();
        Formatter fmt = new Formatter();
        Calendar c = Calendar.getInstance();
        c.setTime(comm.getAuthor().getDate());

        tv_msg.setText(comm.getMessage());
        tv_hash.setText(list.get(position).getSha().substring(0,7));
        tv_date.setText(fmt.format("%td.%tm.%tY %tH:%tM", c, c, c,c,c).toString());
        tv_owner.setText(comm.getAuthor().getName());

        return view;
    }
}
