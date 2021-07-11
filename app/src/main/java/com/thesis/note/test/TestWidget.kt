package com.thesis.note.test

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.thesis.note.R
import com.thesis.note.activity.TextEditorActivityOld
import com.thesis.note.database.AppDatabase
import com.thesis.note.database.entity.Note
import com.thesis.note.database.entity.Widget
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class TestWidget : AppWidgetProvider() {




    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {

       // if(bd==null){
       //     bd = AppDatabase(context)
       // }
        GlobalScope.launch {
           // Notes = bd?.noteDao()?.getAll()
          //  Widgets = bd?.widgetDao()?.getAll()

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }

        }







    }


    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {

    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
    }


    companion object {
        fun updateData(){

        }
        var bd : AppDatabase? = null
        var Notes : List<Note>? =null
        var Widgets : List<Widget>? = null
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {

           // val widgetText = context.getString(R.string.appwidget_textEx)
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.test_widget)
           // views.setTextViewText(R.id.appwidget_text, widgetText)


            val widget:Widget? = Widgets?.find { s -> s.WidgetID == appWidgetId  }
            val note:Note? = Notes?.find { s-> s.IdNote == widget?.NoteId }
            //views.setTextViewText(R.id.appwidget_text,note?.Content )



            val intent = Intent(context, TextEditorActivityOld::class.java)
            intent.putExtra("widgetID",appWidgetId)
            intent.putExtra("noteID", note?.IdNote)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)
            views.setOnClickPendingIntent(R.id.test_widget, pendingIntent)




            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

