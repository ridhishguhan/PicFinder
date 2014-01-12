package com.riddimon.pickpix;

import static android.widget.ImageView.ScaleType.CENTER_CROP;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.riddimon.pickpix.api.ImageResult;
import com.squareup.picasso.Picasso;

final class SampleGridViewAdapter extends CursorAdapter {

	public SampleGridViewAdapter(Context context, Cursor cur) {
		super(context, cur, true);
	}

	@Override
	public void bindView(View convertView, Context context, Cursor cursor) {
		SquaredImageView view = (SquaredImageView) convertView;
		ImageResult res = ImageResult.fromCursor(cursor);

		// Trigger the download of the URL asynchronously into the image view.
		Picasso.with(context) //
				.load(res.thumbUrl) //
				.placeholder(R.drawable.placeholder) //
				.error(R.drawable.error) //
				.fit() //
				.into(view);

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup root) {
		SquaredImageView view = new SquaredImageView(context);
		view.setScaleType(CENTER_CROP);
		return view;
	}
}
