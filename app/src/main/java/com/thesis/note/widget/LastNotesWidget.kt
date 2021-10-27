package com.thesis.note.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.widget.RemoteViews
import com.thesis.note.R
import com.thesis.note.activity.MainActivity
import com.thesis.note.activity.NoteViewerActivity
import com.thesis.note.database.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Functionality of last notes widget.
 */
class LastNotesWidget : AppWidgetProvider() {
    private val actionOpenApp = "actionOpenApp"
    private val actionOpenNote0 = "actionOpenNote0"
    private val actionOpenNote1 = "actionOpenNote1"
    private val actionOpenNote2 = "actionOpenNote2"
    private val actionOpenNote3 = "actionOpenNote3"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val db = AppDatabase(context)
        GlobalScope.launch {
                val allNotes = db.noteDao().getAll().run { sortedWith { x, y -> y.Date?.compareTo(x.Date).let { it ?: -1 } }
            }
            for (appWidgetId in appWidgetIds) {
                //get views
                val views = RemoteViews(context.packageName, R.layout.widget_last_notes)
                //set views
                views.setOnClickPendingIntent(R.id.widget_last_note_title, PendingIntent.getBroadcast(context, 0, Intent(context, LastNotesWidget::class.java).setAction(actionOpenApp), PendingIntent.FLAG_IMMUTABLE))
                if(allNotes.count()>0) {
                    views.setTextViewText(R.id.widget_last_note_0, allNotes[0].Name)
                    views.setOnClickPendingIntent(
                        R.id.widget_last_note_0,
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, LastNotesWidget::class.java).setAction(actionOpenNote0),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
                else{
                    views.setTextViewText(R.id.widget_last_note_0, "")
                }
                if(allNotes.count()>1) {
                    views.setTextViewText(R.id.widget_last_note_1, allNotes[1].Name)
                    views.setOnClickPendingIntent(
                        R.id.widget_last_note_1,
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, LastNotesWidget::class.java).setAction(actionOpenNote1),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
                else{
                    views.setTextViewText(R.id.widget_last_note_1, "")
                }
                if(allNotes.count()>2) {
                    views.setTextViewText(R.id.widget_last_note_2, allNotes[2].Name)
                    views.setOnClickPendingIntent(
                        R.id.widget_last_note_2,
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, LastNotesWidget::class.java).setAction(actionOpenNote2),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
                else{
                    views.setTextViewText(R.id.widget_last_note_2, "")
                }
                if(allNotes.count()>3) {
                    views.setTextViewText(R.id.widget_last_note_3, allNotes[3].Name)
                    views.setOnClickPendingIntent(
                        R.id.widget_last_note_3,
                        PendingIntent.getBroadcast(
                            context,
                            0,
                            Intent(context, LastNotesWidget::class.java).setAction(actionOpenNote3),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                    )
                }
                else{
                    views.setTextViewText(R.id.widget_last_note_3, "")
                }
                //update widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when {
            intent?.action.equals(actionOpenApp) -> {
                context?.startActivity(Intent(context,MainActivity::class.java).apply {  flags = FLAG_ACTIVITY_NEW_TASK})
            }
            intent?.action.equals(actionOpenNote0) -> {
                openNoteViewerActivity(context, 0)
            }
            intent?.action.equals(actionOpenNote1) -> {
                openNoteViewerActivity(context, 1)
            }
            intent?.action.equals(actionOpenNote2) -> {
                openNoteViewerActivity(context, 2)
            }
            intent?.action.equals(actionOpenNote3) -> {
                openNoteViewerActivity(context, 3)
            }
            else -> {
                super.onReceive(context, intent)
            }
        }
    }

    private fun openNoteViewerActivity(context: Context?, noteNumber: Int){
        val db = context?.let { AppDatabase(it) }
        GlobalScope.launch {
            val allNotes = db?.noteDao()?.getAll()?.run { sortedWith { x, y -> y.Date?.compareTo(x.Date).let { it ?: -1 } } }
            if(allNotes != null && noteNumber < allNotes.count() )
                context.startActivity(Intent(context,NoteViewerActivity::class.java).apply {  flags = FLAG_ACTIVITY_NEW_TASK; putExtra("noteID", allNotes[noteNumber].IdNote)})
        }
    }

    companion object{
        fun updateAllWidgets(context: Context){
            val manager = AppWidgetManager.getInstance(context)
            LastNotesWidget().onUpdate(context,manager,manager.getAppWidgetIds(ComponentName(context, LastNotesWidget::class.java)))
        }
    }

}
