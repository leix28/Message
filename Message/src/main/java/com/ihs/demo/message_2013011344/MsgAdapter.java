package com.ihs.demo.message_2013011344;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ihs.account.api.account.HSAccountManager;
import com.ihs.app.framework.HSApplication;
import com.ihs.message_2013011344.R;
import com.ihs.message_2013011344.types.HSAudioMessage;
import com.ihs.message_2013011344.types.HSBaseMessage;
import com.ihs.message_2013011344.types.HSImageMessage;
import com.ihs.message_2013011344.types.HSLocationMessage;
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
    public View getView(int position, View convertView, final ViewGroup parent) {
        HSBaseMessage msg = getItem(position);
        View view;
        final ViewHolder viewHolder;

        if (convertView == null) {
            view = LayoutInflater.from(getContext()).inflate(resourceId, null);
            viewHolder = new ViewHolder();
            viewHolder.leftLayout = (LinearLayout) view.findViewById(R.id.left_layout);
            viewHolder.rightLayout = (LinearLayout) view.findViewById(R.id.right_layout);
            viewHolder.leftMsgText = (TextView) view.findViewById(R.id.left_msg);
            viewHolder.rightMsgText = (TextView) view.findViewById(R.id.right_msg);
            viewHolder.leftStatus = (TextView) view.findViewById(R.id.chat_left_status);
            viewHolder.rightStatus = (TextView) view.findViewById(R.id.chat_right_status);
            viewHolder.leftMsgImage = (ImageView) view.findViewById(R.id.left_image);
            viewHolder.rightMsgImage = (ImageView) view.findViewById(R.id.right_image);
            view.setTag(viewHolder);
        } else {
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
        if (msg.getTo().equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
            viewHolder.leftLayout.setVisibility(View.VISIBLE);
            viewHolder.leftStatus.setVisibility(View.VISIBLE);
            HSBaseMessage.HSMessageStatus st = msg.getStatus();
            if (st == HSBaseMessage.HSMessageStatus.UNREAD) st = HSBaseMessage.HSMessageStatus.READ;
            viewHolder.leftStatus.setText(st.toString());
            viewHolder.rightLayout.setVisibility(View.GONE);
            viewHolder.rightStatus.setVisibility(View.GONE);
            if (msg.getType() == HSMessageType.TEXT) {
                viewHolder.leftMsgText.setText(((HSTextMessage) msg).getText().toString());
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setVisibility(View.GONE);
                viewHolder.leftMsgText.setOnClickListener(null);
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
                viewHolder.leftMsgText.setOnClickListener(null);
            } else if (msg.getType() == HSMessageType.AUDIO) {
                Uri uri;
                final HSAudioMessage adimsg = (HSAudioMessage)msg;
                if (adimsg.getMediaStatus() == HSBaseMessage.HSMessageMediaStatus.TO_DOWNLOAD) {
                    adimsg.download();
                }
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setVisibility(View.GONE);
                viewHolder.leftMsgText.setText("[轻按来播放]" + (adimsg.getDuration() < 3 ? "?" : adimsg.getDuration()) + "秒");
                viewHolder.leftMsgText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (adimsg.getMediaStatus() == HSBaseMessage.HSMessageMediaStatus.DOWNLOADED) {
                            MediaPlayer player = MediaPlayer.create(getContext(), Uri.fromFile(new File(adimsg.getAudioFilePath())));
                            player.start();
                        } else {
                            Toast.makeText(getContext(), "Downloading", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            } else if (msg.getType() == HSMessageType.LOCATION) {
                final  HSLocationMessage locmsg = (HSLocationMessage)msg;
                viewHolder.leftMsgImage.setVisibility(View.GONE);
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgText.setText("[轻按查看地点]");
                viewHolder.leftMsgText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), MapActivity.class);
                        intent.putExtra("latitude", locmsg.getLatitude());
                        intent.putExtra("longitude", locmsg.getLongitude());
                        getContext().startActivity(intent);
                    }
                });

            }  else {
                viewHolder.leftMsgText.setText(msg.toString());
                viewHolder.leftMsgText.setVisibility(View.VISIBLE);
                viewHolder.leftMsgImage.setVisibility(View.GONE);
                viewHolder.leftMsgText.setOnClickListener(null);
            }
        } else if (msg.getFrom().equals(HSAccountManager.getInstance().getMainAccount().getMID())) {
            viewHolder.rightLayout.setVisibility(View.VISIBLE);
            viewHolder.rightStatus.setVisibility(View.VISIBLE);
            viewHolder.leftLayout.setVisibility(View.GONE);
            viewHolder.leftStatus.setVisibility(View.GONE);
            viewHolder.rightStatus.setText(msg.getStatus().toString());

            if (msg.getType() == HSMessageType.TEXT) {
                viewHolder.rightMsgText.setText(((HSTextMessage)msg).getText().toString());
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgImage.setVisibility(View.GONE);
                viewHolder.rightMsgText.setOnClickListener(null);

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
                viewHolder.rightMsgText.setOnClickListener(null);

            } else if (msg.getType() == HSMessageType.AUDIO) {
                final HSAudioMessage adimsg = (HSAudioMessage)msg;
                viewHolder.rightMsgImage.setVisibility(View.GONE);
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgText.setText("[Tap for Audio] " + (int) ((HSAudioMessage) msg).getDuration() + " sec");
                viewHolder.rightMsgText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MediaPlayer player = MediaPlayer.create(getContext(), Uri.fromFile(new File(adimsg.getAudioFilePath())));
                        player.start();
                    }
                });
            } else if (msg.getType() == HSMessageType.LOCATION) {
                final  HSLocationMessage locmsg = (HSLocationMessage)msg;
                viewHolder.rightMsgImage.setVisibility(View.GONE);
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgText.setText("[Tap for Location]");
                viewHolder.rightMsgText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), MapActivity.class);
                        intent.putExtra("latitude", locmsg.getLatitude());
                        intent.putExtra("longitude", locmsg.getLongitude());
                        getContext().startActivity(intent);
                    }
                });

            } else {
                viewHolder.rightMsgText.setText(msg.toString());
                viewHolder.rightMsgText.setVisibility(View.VISIBLE);
                viewHolder.rightMsgImage.setVisibility(View.GONE);
                viewHolder.rightMsgText.setOnClickListener(null);
            }
        }
        return view;
    }

    class ViewHolder {
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMsgText, leftStatus;
        TextView rightMsgText, rightStatus;
        ImageView leftMsgImage;
        ImageView rightMsgImage;

    }
}
