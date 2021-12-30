package com.thesis.note.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import com.thesis.note.Constants
import com.thesis.note.R
import com.thesis.note.activity.TextWidgetEditorActivity
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.Color
import com.thesis.note.database.ColorConverter
import com.thesis.note.database.entity.TextWidget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 *
 */
class NoteWidget : AppWidgetProvider() {
    private val actionEditNote = "actionEditNote"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        //load data
        val db = AppDatabase(context)

        GlobalScope.launch {
            val listOfWidgetData = db.textWidgetDao().getAll()
            for (appWidgetId in appWidgetIds) {
                //get data for widget or create new data
                val dataWidget = listOfWidgetData.find { it.WidgetId == appWidgetId }
                val views = RemoteViews(context.packageName, R.layout.widget_note)
                var newWidget = -1
                if(dataWidget == null){
                    newWidget = db.textWidgetDao().insert(TextWidget(0, appWidgetId,"",Constants.TEXT_SIZE_SMALL, Color.Yellow, Color.Black)).toInt()
                    views.setInt(R.id.widget_note_text_layout, "setBackgroundColor", ContextCompat.getColor(context, ColorConverter.enumToColor(Color.Yellow))  )
                } else {
                    //load data
                    views.setTextViewText(R.id.widget_note_text,dataWidget.Content)
                    views.setTextColor(R.id.widget_note_text,ContextCompat.getColor(context, ColorConverter.enumToColor(dataWidget.FontColor)))
                    views.setTextViewTextSize(R.id.widget_note_text,TypedValue.COMPLEX_UNIT_DIP , dataWidget.Size)
                    //views
                    views.setInt(R.id.widget_note_text_layout, "setBackgroundColor", ContextCompat.getColor(context, ColorConverter.enumToColor(dataWidget.Color)))
                }
                views.setOnClickPendingIntent(
                    R.id.widget_note_text,
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        Intent(context, NoteWidget::class.java).apply {
                            putExtra("textWidgetID", dataWidget?.IdTextWidget ?: newWidget)
                            action = actionEditNote
                            data = Uri.parse(this.toUri(Intent.URI_INTENT_SCHEME))
                        },
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                )


                //update widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action.equals(actionEditNote))
        context?.startActivity(Intent(context,TextWidgetEditorActivity::class.java).apply { flags = FLAG_ACTIVITY_NEW_TASK; putExtra("textWidgetID",intent?.extras?.getInt("textWidgetID"))})
    else
            super.onReceive(context, intent)
    }

    companion object{
        fun updateAllWidgets(context: Context){
            val manager = AppWidgetManager.getInstance(context)
            NoteWidget().onUpdate(context,manager,manager.getAppWidgetIds(ComponentName(context, NoteWidget::class.java)))
        }
    }
}
