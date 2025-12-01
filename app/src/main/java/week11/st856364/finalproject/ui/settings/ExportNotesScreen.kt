package week11.st856364.finalproject.ui.settings

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import week11.st856364.finalproject.ui.notes.NotesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportNotesScreen(
    notesViewModel: NotesViewModel,
    onBack: () -> Unit
) {
    val notes by notesViewModel.notes.collectAsState()
    val context = LocalContext.current

    /* ------------------------------------------------------------------
       TXT Export 
    ------------------------------------------------------------------ */
    val txtLauncher = rememberLauncherForActivityResult(
        CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val text = buildString {
            append("📄 NoteSync Export File (TXT)\n\n")
            notes.forEach { note ->
                append("Title: ${note.title}\n")
                append("Content: ${note.content}\n")
                append("Language: ${note.languageCode}\n")
                append("Pinned: ${note.pinned}\n")
                append("───────────────────────────\n\n")
            }
        }
        context.contentResolver.openOutputStream(uri)?.use {
            it.write(text.toByteArray())
        }
    }

    /* ------------------------------------------------------------------
       PDF Export 
    ------------------------------------------------------------------ */
    val pdfLauncher = rememberLauncherForActivityResult(
        CreateDocument("application/pdf")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        val pdf = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842

        val titlePaint = Paint().apply {
            textSize = 22f
            isFakeBoldText = true
        }
        val textPaint = Paint().apply {
            textSize = 14f
        }

        var y = 60f
        var pageNumber = 1

        fun newPage(): PdfDocument.Page {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdf.startPage(pageInfo)
            page.canvas.drawText("📘 NoteSync – Notes Export", 40f, 40f, titlePaint)
            y = 70f
            return page
        }

        var page = newPage()
        var canvas = page.canvas

        notes.forEach { note ->
            if (y > pageHeight - 80) {
                pdf.finishPage(page)
                pageNumber++
                page = newPage()
                canvas = page.canvas
            }

            canvas.drawText("Title: ${note.title}", 40f, y, textPaint)
            y += 20

            note.content.chunked(85).forEach { line ->
                if (y > pageHeight - 60) {
                    pdf.finishPage(page)
                    pageNumber++
                    page = newPage()
                    canvas = page.canvas
                }
                canvas.drawText(line, 40f, y, textPaint)
                y += 18
            }

            canvas.drawText("Language: ${note.languageCode}", 40f, y, textPaint)
            y += 16

            canvas.drawText("Pinned: ${note.pinned}", 40f, y, textPaint)
            y += 26

            canvas.drawLine(40f, y, pageWidth - 40f, y, Paint())
            y += 20
        }

        pdf.finishPage(page)

        context.contentResolver.openOutputStream(uri)?.use {
            pdf.writeTo(it)
        }
        pdf.close()
    }

    /* ------------------------------------------------------------------
       BEAUTIFUL EXPORT SCREEN UI
    ------------------------------------------------------------------ */
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF3EFFF), Color(0xFFF8F5FF))
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            /* ------------------ HEADER CARD ------------------ */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEEE5FF)
                ),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(Color(0xFF7E57C2), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Spacer(Modifier.width(14.dp))

                        Column {
                            Text("Export Your Notes", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "Save as TXT or PDF for backups or sharing.",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            }

            /* ------------------ TXT EXPORT ------------------ */
            Button(
                onClick = { txtLauncher.launch("notes_export.txt") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Export as TXT File", color = Color.White, fontSize = 16.sp)
            }

            Text(
                "TXT export creates a lightweight, readable text file with all your notes, ideal for sharing or uploading.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            /* ------------------ PDF EXPORT ------------------ */
            Button(
                onClick = { pdfLauncher.launch("notes_export.pdf") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF512DA8))
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Export as PDF (Recommended)", color = Color.White, fontSize = 16.sp)
            }

            Text(
                "The PDF export generates a beautifully formatted document with proper structure, titles, spacing, and page breaks — perfect for printing or archiving.",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(10.dp))

            /* ------------------ FOOTER INFO ------------------ */
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF0ECFF)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("What’s included:", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    Text("• Note titles", fontSize = 13.sp)
                    Text("• Entire note content", fontSize = 13.sp)
                    Text("• Selected color & language", fontSize = 13.sp)
                    Text("• Pinned status", fontSize = 13.sp)
                    Text("• Section dividers", fontSize = 13.sp)
                    Text("• Auto page wrapping in PDF", fontSize = 13.sp)
                }
            }
        }
    }
}
