from pathlib import Path
from reportlab.pdfgen import canvas
from reportlab.lib.colors import HexColor
from pypdf import PdfReader

out = Path(__file__).parent
c = canvas.Canvas(str(out / 'donatrack-logo-vectorial.pdf'), pagesize=(1120,1120))
c.setTitle('DonaTrack - logo vectorial reconstruido')
c.setAuthor('DonaTrack')
c.translate(-240,1360)
c.scale(1,-1)

def polygon(points, color):
    p = c.beginPath()
    p.moveTo(*points[0])
    for xy in points[1:]: p.lineTo(*xy)
    p.close()
    c.setFillColor(HexColor(color))
    c.drawPath(p, fill=1, stroke=0)

# Todas las superficies son trazados, no imagenes incrustadas.
polygon([(427,725),(800,580),(1173,725),(800,870)], '#956035')
polygon([(427,725),(800,870),(800,1305),(427,1164)], '#C5915C')
polygon([(800,870),(1173,725),(1173,1164),(800,1305)], '#B47F4D')
polygon([(427,725),(800,870),(680,1053),(427,949)], '#AE7947')
polygon([(800,870),(1173,725),(1173,949),(919,1053)], '#9E693D')
polygon([(310,598),(688,456),(800,580),(427,725)], '#C69360')
polygon([(800,580),(913,456),(1290,598),(1173,725)], '#C99561')
polygon([(427,725),(800,870),(680,1000),(310,858)], '#D29E6A')
polygon([(800,870),(1173,725),(1290,858),(919,1000)], '#D19D69')

# Corazon posterior, desplazado para conservar el volumen original.
p = c.beginPath()
p.moveTo(834,746)
p.curveTo(774,722,644,666,598,604)
p.curveTo(558,551,571,470,618,417)
p.curveTo(671,356,706,359,766,394)
p.curveTo(805,297,868,281,908,300)
p.lineTo(971,331)
p.curveTo(1040,382,1005,533,834,746)
p.close()
c.setFillColor(HexColor('#CA3D3B'))
c.drawPath(p,fill=1,stroke=0)

p = c.beginPath()
p.moveTo(834,746)
p.curveTo(794,730,676,682,651,614)
p.curveTo(627,550,660,467,719,425)
p.curveTo(768,389,807,404,830,443)
p.curveTo(856,364,928,308,977,336)
p.curveTo(1068,388,991,563,834,746)
p.close()
c.setFillColor(HexColor('#F75953'))
c.drawPath(p,fill=1,stroke=0)
c.showPage()
c.save()
reader=PdfReader(out/'donatrack-logo-vectorial.pdf')
assert len(reader.pages)==1
assert len(reader.pages[0].images)==0, 'El master debe contener solo vectores'
print('PDF vectorial: una pagina, cero imagenes incrustadas')
