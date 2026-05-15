from fpdf import FPDF
from fpdf.enums import XPos, YPos

class ProjectReport(FPDF):
    def header(self):
        self.set_font('helvetica', 'B', 15)
        self.cell(0, 10, 'MadhuMarga Agri - Project Documentation', 0, new_x=XPos.LMARGIN, new_y=YPos.NEXT, align='C')
        self.ln(5)

    def footer(self):
        self.set_y(-15)
        self.set_font('helvetica', 'I', 8)
        self.cell(0, 10, f'Page {self.page_no()}', 0, 0, 'C')

    def chapter_title(self, title):
        self.set_font('helvetica', 'B', 12)
        self.set_fill_color(200, 220, 255)
        self.cell(0, 6, title, 0, new_x=XPos.LMARGIN, new_y=YPos.NEXT, align='L', fill=True)
        self.ln(4)

    def chapter_body(self, body):
        self.set_font('helvetica', '', 11)
        self.multi_cell(0, 5, body)
        self.ln()

def generate_report():
    pdf = ProjectReport()
    pdf.add_page()

    # Project Overview
    pdf.chapter_title('Project Title')
    pdf.chapter_body('MadhuMarga Agri')

    pdf.chapter_title('Short Description')
    pdf.chapter_body('A specialized Android application designed for beekeepers to streamline apiary management, including hive registration, health monitoring, and harvest tracking.')

    pdf.chapter_title('GitHub URL')
    pdf.chapter_body('https://github.com/marutigoudra/madhumarga_agri')

    # Problem Statement
    pdf.chapter_title('Problem Statement')
    pdf.chapter_body(
        "Traditional beekeeping often relies on manual or fragmented record-keeping for hive health, "
        "inspection cycles, and harvest yields. This makes it difficult to track long-term trends or "
        "identify issues across multiple apiaries. MadhuMarga solves this by providing a centralized, "
        "digital dashboard to monitor hive lifecycle, floral availability, and productivity in real-time."
    )

    # Technologies
    pdf.chapter_title('Technologies Used')
    pdf.chapter_body(
        "- Android (Kotlin)\n"
        "- Room Persistence Library (SQLite)\n"
        "- Jetpack Components (ViewModel, LiveData)\n"
        "- Material Design\n"
        "- Gradle (Kotlin DSL)"
    )

    # Core Features
    pdf.chapter_title('Key Features')
    pdf.chapter_body(
        "- Hive Management: Digital inventory and registration of hives.\n"
        "- Inspection Logs: Health tracking, queen status, and pest monitoring.\n"
        "- Harvest Tracker: Quantitative logging of honey and wax yields.\n"
        "- Flora Calendar: Tracking local floral cycles for optimized nectar flow planning.\n"
        "- Offline Support: Fully functional without internet connectivity using Room Database."
    )

    pdf.output('MadhuMarga_Project_Report.pdf')
    print("PDF generated successfully: MadhuMarga_Project_Report.pdf")

if __name__ == "__main__":
    generate_report()
