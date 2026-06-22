package com.pay.eeaapp.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.pay.eeaapp.domain.models.Project
import com.pay.eeaapp.domain.models.ProjectDocument
import com.pay.eeaapp.domain.models.ReviewComment
import org.apache.poi.ss.usermodel.*
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ProjectExporter {

    private val dateFormatter = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
    private val shortDate    = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())

    fun export(
        context: Context,
        projects: List<Project>,
        documents: Map<String, List<ProjectDocument>>,
        reviews:   Map<String, List<ReviewComment>>
    ): Uri {
        val wb = XSSFWorkbook()

        buildSummarySheet(wb, projects)
        buildProjectsSheet(wb, projects)
        buildDocumentsSheet(wb, projects, documents)
        buildReviewsSheet(wb, projects, reviews)

        val file = File(context.cacheDir, "EEA_Projects_Export_${shortDate.format(Date())}.xlsx")
        FileOutputStream(file).use { wb.write(it) }
        wb.close()

        return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
    }

    private fun buildSummarySheet(wb: XSSFWorkbook, projects: List<Project>) {
        val sheet = wb.createSheet("Summary")
        sheet.setColumnWidth(0, 10000)
        sheet.setColumnWidth(1, 5000)

        val titleStyle = titleStyle(wb)
        val headerStyle = headerStyle(wb)
        val dataStyle   = dataStyle(wb)
        val accentStyle = accentStyle(wb)

        var row = 0

        sheet.createRow(row++).apply {
            createCell(0).apply { setCellValue("EEA Project Applications Export"); cellStyle = titleStyle }
        }
        sheet.createRow(row++).apply {
            createCell(0).apply {
                setCellValue("Generated: ${dateFormatter.format(Date())}")
                cellStyle = dataStyle
            }
        }
        sheet.addMergedRegion(CellRangeAddress(0, 0, 0, 1))
        row++

        listOf(
            "Total Projects"      to projects.size,
            "Submitted"           to projects.count { it.status.name == "SUBMITTED" },
            "Under Review"        to projects.count { it.status.name == "UNDER_REVIEW" },
            "Amendments Required" to projects.count { it.status.name == "AMENDMENTS_REQUIRED" },
            "Approved"            to projects.count { it.status.name == "APPROVED" },
            "Rejected"            to projects.count { it.status.name == "REJECTED" },
        ).forEach { (label, count) ->
            sheet.createRow(row++).apply {
                createCell(0).apply { setCellValue(label); cellStyle = headerStyle }
                createCell(1).apply { setCellValue(count.toDouble()); cellStyle = accentStyle }
            }
        }
    }

    private fun buildProjectsSheet(wb: XSSFWorkbook, projects: List<Project>) {
        val sheet = wb.createSheet("Projects")
        val headers = listOf(
            "Project ID", "Title", "Status", "Proponent Name",
            "Company", "Description", "Latitude", "Longitude", "Submitted At"
        )
        val colWidths = listOf(8000, 12000, 6000, 8000, 8000, 20000, 5000, 5000, 8000)
        headers.forEachIndexed { i, _ -> sheet.setColumnWidth(i, colWidths[i]) }

        val headerStyle = headerStyle(wb)
        val dataStyle   = dataStyle(wb)
        val altStyle    = altDataStyle(wb)

        sheet.createRow(0).apply {
            headers.forEachIndexed { i, h ->
                createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }

        projects.forEachIndexed { idx, p ->
            val style = if (idx % 2 == 0) dataStyle else altStyle
            sheet.createRow(idx + 1).apply {
                createCell(0).apply { setCellValue(p.id);             cellStyle = style }
                createCell(1).apply { setCellValue(p.title);          cellStyle = style }
                createCell(2).apply { setCellValue(p.status.name.replace('_', ' ')); cellStyle = style }
                createCell(3).apply { setCellValue(p.proponentName);  cellStyle = style }
                createCell(4).apply { setCellValue(p.companyName);    cellStyle = style }
                createCell(5).apply { setCellValue(p.description);    cellStyle = style }
                createCell(6).apply { setCellValue(p.latitude);       cellStyle = style }
                createCell(7).apply { setCellValue(p.longitude);      cellStyle = style }
                createCell(8).apply {
                    setCellValue(dateFormatter.format(Date(p.createdAt)))
                    cellStyle = style
                }
            }
        }

        sheet.createFreezePane(0, 1)
    }

    private fun buildDocumentsSheet(
        wb: XSSFWorkbook,
        projects: List<Project>,
        documents: Map<String, List<ProjectDocument>>
    ) {
        val sheet = wb.createSheet("Documents")
        val headers = listOf("Project ID", "Project Title", "Document ID", "File Name", "File URL")
        val colWidths = listOf(8000, 12000, 8000, 12000, 20000)
        headers.forEachIndexed { i, _ -> sheet.setColumnWidth(i, colWidths[i]) }

        val headerStyle = headerStyle(wb)
        val dataStyle   = dataStyle(wb)
        val altStyle    = altDataStyle(wb)

        sheet.createRow(0).apply {
            headers.forEachIndexed { i, h ->
                createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }

        var rowIdx = 1
        projects.forEach { project ->
            val docs = documents[project.id] ?: emptyList()
            if (docs.isEmpty()) {
                val style = if (rowIdx % 2 == 0) altStyle else dataStyle
                sheet.createRow(rowIdx++).apply {
                    createCell(0).apply { setCellValue(project.id);    cellStyle = style }
                    createCell(1).apply { setCellValue(project.title); cellStyle = style }
                    createCell(2).apply { setCellValue("—");           cellStyle = style }
                    createCell(3).apply { setCellValue("No documents"); cellStyle = style }
                    createCell(4).apply { setCellValue("—");           cellStyle = style }
                }
            } else {
                docs.forEach { doc ->
                    val style = if (rowIdx % 2 == 0) altStyle else dataStyle
                    sheet.createRow(rowIdx++).apply {
                        createCell(0).apply { setCellValue(project.id);    cellStyle = style }
                        createCell(1).apply { setCellValue(project.title); cellStyle = style }
                        createCell(2).apply { setCellValue(doc.id);        cellStyle = style }
                        createCell(3).apply { setCellValue(doc.fileName);  cellStyle = style }
                        createCell(4).apply { setCellValue(doc.fileUrl);   cellStyle = style }
                    }
                }
            }
        }

        sheet.createFreezePane(0, 1)
    }

    private fun buildReviewsSheet(
        wb: XSSFWorkbook,
        projects: List<Project>,
        reviews: Map<String, List<ReviewComment>>
    ) {
        val sheet = wb.createSheet("Review History")
        val headers = listOf("Project ID", "Project Title", "Review ID", "Comment", "Date")
        val colWidths = listOf(8000, 12000, 8000, 25000, 8000)
        headers.forEachIndexed { i, _ -> sheet.setColumnWidth(i, colWidths[i]) }

        val headerStyle = headerStyle(wb)
        val dataStyle   = dataStyle(wb)
        val altStyle    = altDataStyle(wb)

        sheet.createRow(0).apply {
            headers.forEachIndexed { i, h ->
                createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }

        var rowIdx = 1
        projects.forEach { project ->
            val revs = reviews[project.id] ?: emptyList()
            if (revs.isEmpty()) {
                val style = if (rowIdx % 2 == 0) altStyle else dataStyle
                sheet.createRow(rowIdx++).apply {
                    createCell(0).apply { setCellValue(project.id);    cellStyle = style }
                    createCell(1).apply { setCellValue(project.title); cellStyle = style }
                    createCell(2).apply { setCellValue("—");           cellStyle = style }
                    createCell(3).apply { setCellValue("No reviews");  cellStyle = style }
                    createCell(4).apply { setCellValue("—");           cellStyle = style }
                }
            } else {
                revs.forEach { review ->
                    val style = if (rowIdx % 2 == 0) altStyle else dataStyle
                    sheet.createRow(rowIdx++).apply {
                        createCell(0).apply { setCellValue(project.id);    cellStyle = style }
                        createCell(1).apply { setCellValue(project.title); cellStyle = style }
                        createCell(2).apply { setCellValue(review.id);     cellStyle = style }
                        createCell(3).apply { setCellValue(review.comment); cellStyle = style }
                        createCell(4).apply {
                            setCellValue(dateFormatter.format(Date(review.createdAt)))
                            cellStyle = style
                        }
                    }
                }
            }
        }

        sheet.createFreezePane(0, 1)
    }

    private fun titleStyle(wb: XSSFWorkbook) = wb.createCellStyle().apply {
        setFont(wb.createFont().apply {
            bold      = true
            fontHeightInPoints = 14
            color     = IndexedColors.WHITE.index
        })
        fillForegroundColor = IndexedColors.DARK_GREEN.index
        fillPattern         = FillPatternType.SOLID_FOREGROUND
        alignment           = HorizontalAlignment.LEFT
        verticalAlignment   = VerticalAlignment.CENTER
    }

    private fun headerStyle(wb: XSSFWorkbook) = wb.createCellStyle().apply {
        setFont(wb.createFont().apply {
            bold  = true
            color = IndexedColors.WHITE.index
            fontHeightInPoints = 10
        })
        fillForegroundColor = IndexedColors.DARK_GREEN.index
        fillPattern         = FillPatternType.SOLID_FOREGROUND
        alignment           = HorizontalAlignment.CENTER
        verticalAlignment   = VerticalAlignment.CENTER
        setBorder(this)
    }

    private fun dataStyle(wb: XSSFWorkbook) = wb.createCellStyle().apply {
        setFont(wb.createFont().apply { fontHeightInPoints = 10 })
        alignment         = HorizontalAlignment.LEFT
        verticalAlignment = VerticalAlignment.TOP
        wrapText          = true
        setBorder(this)
    }

    private fun altDataStyle(wb: XSSFWorkbook) = wb.createCellStyle().apply {
        setFont(wb.createFont().apply { fontHeightInPoints = 10 })
        fillForegroundColor = IndexedColors.LIGHT_GREEN.index
        fillPattern         = FillPatternType.SOLID_FOREGROUND
        alignment           = HorizontalAlignment.LEFT
        verticalAlignment   = VerticalAlignment.TOP
        wrapText            = true
        setBorder(this)
    }

    private fun accentStyle(wb: XSSFWorkbook) = wb.createCellStyle().apply {
        setFont(wb.createFont().apply {
            bold  = true
            color = IndexedColors.DARK_GREEN.index
            fontHeightInPoints = 11
        })
        alignment         = HorizontalAlignment.CENTER
        verticalAlignment = VerticalAlignment.CENTER
        setBorder(this)
    }

    private fun setBorder(style: CellStyle) {
        style.borderBottom = BorderStyle.THIN
        style.borderTop    = BorderStyle.THIN
        style.borderLeft   = BorderStyle.THIN
        style.borderRight  = BorderStyle.THIN
    }
}