package com.github.mobile.android;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mobile.android.R.layout;
import com.github.mobile.android.repo.RepoBrowseActivity;
import com.github.mobile.android.util.Avatar;
import com.google.inject.Inject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.UserService;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

/**
 * Home screen activity
 */
public class HomeActivity extends RoboActivity {

    private class OrgListAdapter extends ArrayAdapter<User> {

        /**
         * @param objects
         */
        public OrgListAdapter(List<User> objects) {
            super(HomeActivity.this, R.layout.org_item, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final LinearLayout view = (LinearLayout) HomeActivity.this.getLayoutInflater().inflate(layout.org_item,
                    null);
            User user = getItem(position);
            ((TextView) view.findViewById(R.id.tv_org_name)).setText(user.getLogin());
            Avatar.bind(HomeActivity.this, ((ImageView) view.findViewById(R.id.iv_gravatar)), user.getLogin(),
                    user.getAvatarUrl());
            return view;
        }
    }

    @Inject
    private OrganizationService orgService;

    @Inject
    private UserService userService;

    @InjectView(R.id.ll_progress)
    private LinearLayout progress;

    @InjectView(R.id.ll_orgs)
    private LinearLayout orgsLayout;

    @InjectView(R.id.lv_orgs)
    private ListView orgsList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        orgsList.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> view, View arg1, int position, long id) {
                User user = (User) view.getItemAtPosition(position);
                startActivity(RepoBrowseActivity.createIntent(HomeActivity.this, user));
            }
        });
        loadOrgs();
    }

    private void loadOrgs() {
        progress.setVisibility(View.VISIBLE);
        orgsLayout.setVisibility(View.GONE);
        new RoboAsyncTask<List<User>>(this) {

            public List<User> call() throws Exception {
                List<User> orgs = new ArrayList<User>(orgService.getOrganizations());
                Collections.sort(orgs, new Comparator<User>() {

                    public int compare(User u1, User u2) {
                        return u1.getLogin().compareToIgnoreCase(u2.getLogin());
                    }
                });
                orgs.add(0, userService.getUser());
                return orgs;
            }

            protected void onSuccess(List<User> orgs) throws Exception {
                orgsList.setAdapter(new OrgListAdapter(orgs));
                progress.setVisibility(View.GONE);
                orgsLayout.setVisibility(View.VISIBLE);
            };
        }.execute();
    }
}
