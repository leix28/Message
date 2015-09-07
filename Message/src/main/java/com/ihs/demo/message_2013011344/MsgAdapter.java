package com.ihs.demo.message_2013011344;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.types.HSBaseMessage;
import com.ihs.message_2013011344.types.HSImageMessage;
import com.ihs.message_2013011344.types.HSMessageType;
import com.ihs.message_2013011344.types.HSTextMessage;

import java.io.File;
import java.util.List;

/**
 * Created by LazyLie on 15/9/5.
 */
public class MsgAdapter extends ArrayAdapter<HSBaseMessage> {
    private int resourceId;

    public MsgAdapter(Context context, int textViewResourceId, List<HSBaseMessage> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HSBaseMessage msg = getItem(position);
        View view;
        ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            viewHolder.leftMsgText = (TextView) view.findViewById(R.id.left_msg);
            viewHolder.rightMsgText = (TextView) view.findViewById(R.id.right_msg);
            viewHolder.leftMsgImage = (ImageView) view.findViewById(R.id.left_image);
            viewHolder.rightMsgImage = (ImageView) view.findViewById(R.id.right_image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (msg.getTo().equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.rightLayout.setVisibility(View.GONE);
            if (msg.getType() == HSMessageType.TEXT) {
                viewHolder.leftMsgText.setText(((HSTextMessage)msg).getText().toString());
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setVisibility(View.GONE);
            } else if (msg.getType() == HSMessageType.IMAGE) {
                Uri uri;
                final HSImageMessage imgmsg = (HSImageMessage)msg;
                if (imgmsg.getNormalImageMediaStatus() == HSBaseMessage.HSMessageMediaStatus.TO_DOWNLOAD) {
                    imgmsg.download();
                }
                if (imgmsg.getNormalImageMediaStatus() == HSBaseMessage.HSMessageMediaStatus.DOWNLOADED) {
                    uri = Uri.fromFile(new File(imgmsg.getNormalImageFilePath()));
                } else {
                    final File imageFile = new File(HSApplication.getContext().getCacheDir() + "/" + "loading_img.png");
                    SampleFragment.copy("loading_img.png", imageFile);

                    uri = Uri.fromFile(imageFile);
                }
                viewHolder.leftMsgImage.setImageURI(uri);
                viewHolder.leftMsgText.setVisibility(View.GONE);
                viewHolder.leftMsgImage.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (imgmsg.getNormalImageMediaStatus() == HSBaseMessage.HSMessageMediaStatus.DOWNLOADED) {
                            Intent intent = new Intent(getContext(), ImgActivity.class);
                            intent.putExtra("src", imgmsg.getNormalImageFilePath());
                            getContext().startActivity(intent);
                        }
                    }
                });
            } else {
                viewHolder.leftMsgText.setText(msg.toString());
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setVisibility(View.GONE);
            }
        } else if (msg.getFrom().equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            if (msg.getType() == HSMessageType.TEXT) {
                viewHolder.rightMsgText.setText(((HSTextMessage)msg).getText().toString());
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgImage.setVisibility(View.GONE);
            } else if (msg.getType() == HSMessageType.IMAGE) {
                final HSImageMessage imgmsg = (HSImageMessage)msg;
                viewHolder.rightMsgImage.setImageURI(Uri.fromFile(new File(imgmsg.getNormalImageFilePath())));
                viewHolder.rightMsgText.setVisibility(View.GONE);
                viewHolder.rightMsgImage.setVisibility(View.VISIBLE);

                viewHolder.rightMsgImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), ImgActivity.class);
                        intent.putExtra("src", imgmsg.getNormalImageFilePath());
                        getContext().startActivity(intent);
                    }
                });
            } else {
                viewHolder.rightMsgText.setText(msg.toString());
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgImage.setVisibility(View.GONE);
            }
        }
        return view;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsgText;
        TextView rightMsgText;
        ImageView leftMsgImage;
        ImageView rightMsgImage;
    }
}