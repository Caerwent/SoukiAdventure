 
#include <QImage>
#include <QString>
#include <QRect>
#include <QPoint>
#include <QPainter>

int main(int argc, char *argv[ ])
{
 QList<int> list;

    QString str="8 9 10 9 10 11 "
            "12 13 14 13 14 15 "
            "16 17 18 17 18 19 "
            "12 13 14 13 14 15 "
            "16 17 18 17 18 19 "
            "20 21 22 21 22 23 "
            "13 14 13 14 -1 -1 "
            "17 7 6 18 -1 -1 "
            "13 3 2 14 -1 -1 "
            "17 18 17 18 -1 -1";

    foreach(QString s, str.split(" ")){
        list << s.toInt();
    }


    QImage img(argv[1]);
    img.convertToFormat(QImage::Format_ARGB32);

    QImage result(img.width() * 3/2, img.height() * 5/3, QImage::Format_ARGB32);

    const int cropWidth = 32 * 2;
    const int cropHeight = 32 * 3;

    const int expandedWidth = cropWidth * 3/2;
    const int expandedHeight = cropHeight * 5/3;

    int columns = img.width() / cropWidth;
    int rows = img.height() / cropHeight;

    QPainter p;

    qDebug( argv[1]  );
qDebug( argv[2]  );
qDebug( "row=%d col=%d",rows,columns  );
    for(int r=0; r<rows; r++){
        for(int c=0; c<columns; c++){

            QRect areaRect;
            areaRect.setX(c * cropWidth);
            areaRect.setY(r * cropHeight );
            areaRect.setWidth(cropWidth);
            areaRect.setHeight(cropHeight);

            QImage area = img.copy(areaRect);
            QImage out(16 * 6, 16 * 10, QImage::Format_ARGB32);
            p.begin(&out);

p.setCompositionMode(QPainter::CompositionMode_Clear);
p.fillRect(0,0,16 * 6, 16 * 10, QColor(0,0,0,0));
p.setCompositionMode(QPainter::CompositionMode_SourceOver);
            for(int i=0; i< 10; i++){
                for(int j=0; j< 6; j++){
                    int n = list.at(6 * i + j);

                    QPoint pos(j * 16, i * 16);

                    if(n == -1){
                        

                        continue;
                    }

                    QRect source;
                    source.setX((n%4) * 16);
                    source.setY((n/4) * 16);
                    source.setWidth(16);
                    source.setHeight(16);

                   p.drawImage(pos, area, source);
                }
            }
	    
p.end();
            p.begin(&result);
            p.drawImage(c * expandedWidth, r * expandedHeight, out);
            p.end();
        }
    }

    result.save(argv[2]);
return 0;
}