package com.joe.bibi.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.joe.bibi.R;
import com.joe.bibi.application.BBApplication;
import com.joe.bibi.domain.Contacts;
import com.joe.bibi.utils.CharacterParser;
import com.joe.bibi.utils.CommonUtils;
import com.joe.bibi.utils.PinyinComparator;
import com.joe.bibi.utils.ToastUtils;
import com.joe.bibi.view.SideBar;

import org.xutils.x;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.bmob.im.BmobUserManager;
import cn.bmob.im.bean.BmobChatUser;
import cn.bmob.v3.listener.UpdateListener;

public class ContactActivity extends AppCompatActivity {

    private ListView mListView;
    private SideBar mSidebar;
    private List<Contacts> mContacts;
    private CharacterParser characterParser;
    private myAdapter adapter;
    private SearchView mSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initView();
    }

    private void initView() {
        ActionBar ab=getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mListView = (ListView) findViewById(R.id.lv_contact);
        mSidebar = (SideBar) findViewById(R.id.sb_contact);
        mSearch = (SearchView) findViewById(R.id.search_contacts);
        initContacts();
    }
    private PinyinComparator pinyinComparator;
    //初始化联系人信息
    private void initContacts() {
        characterParser = new CharacterParser();
        mContacts = new ArrayList<Contacts>();
        pinyinComparator=new PinyinComparator();
        for (BmobChatUser user:BBApplication.getInstance().getContactList()) {
            Contacts c=new Contacts();
            c.setUserName(user.getUsername());
            c.setAvatar(user.getAvatar());
            c.setName(user.getNick());
            c.setId(user.getObjectId());
            String pinyin = characterParser.getSelling(user.getNick());
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                c.setSortLetters(sortString.toUpperCase());
            }else{
                c.setSortLetters("#");
            }
            mContacts.add(c);
        }
        /*//测试
        String[] data=getResources().getStringArray(R.array.date);
        for(int i=0;i<data.length;i++){
            Contacts c=new Contacts();
            c.setAvatar(BBApplication.getInstance().getContactList().get(0).getAvatar());
            c.setName(data[i]);
            String pinyin = characterParser.getSelling(data[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();
            // 正则表达式，判断首字母是否是英文字母
            if(sortString.matches("[A-Z]")){
                c.setSortLetters(sortString.toUpperCase());
            }else{
                c.setSortLetters("#");
            }
            mContacts.add(c);
        }*/
        Collections.sort(mContacts, pinyinComparator);
        initAdapterAndListener();
    }

    private void initAdapterAndListener() {
        adapter = new myAdapter(this,mContacts);
        mListView.setAdapter(adapter);

        mSidebar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            @Override
            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }

            }
        });
        mSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterData(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterData(newText);
                return false;
            }
        });
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(ContactActivity.this, UserActivity.class);
                intent.putExtra("username", adapter.getItem(position).getUserName());
                startActivity(intent);
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Contacts c=mContacts.get(position);
                AlertDialog.Builder builder=new AlertDialog.Builder(ContactActivity.this);
                builder.setTitle("删除好友？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //删除会话及聊天记录
                        BmobUserManager.getInstance(ContactActivity.this).deleteContact(c.getId(), new UpdateListener() {
                            @Override
                            public void onSuccess() {
                                ToastUtils.make(ContactActivity.this,"删除成功");
                                BBApplication.getInstance().updateContactList();
                            }

                            @Override
                            public void onFailure(int i, String s) {

                            }
                        });
                        mContacts.remove(position);
                        adapter.update(mContacts);
                        dialog.dismiss();
                        ToastUtils.make(ContactActivity.this, "删除成功");
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return false;
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                CommonUtils.hideKeyBoard(ContactActivity.this);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }
    private void filterData(String filterStr){
        List<Contacts> filterDateList = new ArrayList<Contacts>();

        if(TextUtils.isEmpty(filterStr)){
            filterDateList = mContacts;
        }else{
            filterDateList.clear();
            for(Contacts Contacts : mContacts){
                String name = Contacts.getName();
                if(name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())){
                    filterDateList.add(Contacts);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.update(filterDateList);
    }
    class myAdapter extends BaseAdapter implements SectionIndexer{
        private Activity mActivity;
        private List<Contacts> contactsList;
        public myAdapter(Activity activity,List<Contacts> contactsList){
            this.mActivity=activity;
            this.contactsList=contactsList;
        }
        @Override
        public int getCount() {
            return contactsList.size();
        }

        @Override
        public Contacts getItem(int position) {
            return contactsList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public void update(List<Contacts> list){
            this.contactsList=list;
            notifyDataSetChanged();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Contacts contact=contactsList.get(position);
            ViewHolder holder=null;
            View v=convertView;
            if(v==null){
                v=View.inflate(mActivity,R.layout.item_contact,null);
                holder=new ViewHolder();
                holder.name= (TextView) v.findViewById(R.id.tv_nick_contact);
                holder.avatar= (ImageView) v.findViewById(R.id.iv_avatar_contact);
                holder.indicator= (TextView) v.findViewById(R.id.ll_content_contact);
                v.setTag(holder);
            }else{
                holder= (ViewHolder) v.getTag();
            }
            //根据position获取分类的首字母的Char ascii值
            int section = getSectionForPosition(position);
            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if(position == getPositionForSection(section)){
                holder.indicator.setVisibility(View.VISIBLE);
                holder.indicator.setText(contact.getSortLetters());
            }else{
                holder.indicator.setVisibility(View.GONE);
            }
            x.image().bind(holder.avatar,contact.getAvatar());
            holder.name.setText(contact.getName());
            return v;
        }

        @Override
        public Object[] getSections() {
            return null;
        }

        @Override
        public int getPositionForSection(int sectionIndex) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = contactsList.get(i).getSortLetters();
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == sectionIndex) {
                    return i;
                }
            }

            return -1;
        }

        @Override
        public int getSectionForPosition(int position) {
            return contactsList.get(position).getSortLetters().charAt(0);
        }
    }

    class ViewHolder{
        TextView name;
        ImageView avatar;
        TextView indicator;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
