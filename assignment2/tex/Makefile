GRAPHS=graph1_dot.pdf graph2_dot.pdf
TARGET=assignment2.pdf

all: $(TARGET)

$(TARGET): $(GRAPHS)

clean: cleantmp
	rm -rf $(GRAPHS) $(TARGET)

cleantmp:
	rm -rf *.aux *.log *.out *.toc

%.svg: %.dot
	dot $*.dot -Tsvg -o $*.svg

%.png: %.dot
	dot $*.dot -Tpng -o $*.png

%.ps: %.dot
	dot $*.dot -Tps -o $*.ps

%_dot.pdf: %.dot
	dot $*.dot -Tpdf -o $*_dot.pdf

%.pdf: %.tex
	pdflatex $*.tex
	pdflatex $*.tex
	pdflatex $*.tex
