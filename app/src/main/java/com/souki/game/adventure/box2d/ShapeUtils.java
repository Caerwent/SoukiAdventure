package com.souki.game.adventure.box2d;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.souki.game.adventure.entity.components.CollisionObstacleComponent;

import java.util.ArrayList;

/**
 * Created by gwalarn on 20/11/16.
 */

public class ShapeUtils {
    /**
     * Get projected point of P on line v1,v2. Faster version. (http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html)
     *
     * @return projected point p.
     */
    static public Vector2 getProjectedPointOnLineFast(float vx1, float vy1, float vx2, float vy2, float px, float py) {
        // get dot product of e1, e2
        Vector2 e1 = new Vector2(vx2 - vx1, vy2 - vy1);
        Vector2 e2 = new Vector2(px - vx1, py - vy1);
        double valDp = e1.x * e2.x + e1.y * e2.y;
        // get squared length of e1
        double len2 = e1.x * e1.x + e1.y * e1.y;
        Vector2 p = new Vector2((float) (vx1 + (valDp * e1.x) / len2),
                (float) (vy1 + (valDp * e1.y) / len2));
        return p;
    }

    /**
     * Check if p', the projected point of P onto line v1,v2 is on the line segment.(http://www.sunshine2k.de/coding/java/PointOnLine/PointOnLine.html)
     *
     * @return -1 if p' before v1
     * 0 if p' is v1
     * 1 if p' is between v1 and v2
     * 2 if p' is v2
     * 3 if p' is after v2
     */
    static public int isProjectedPointOnLineSegment(float vx1, float vy1, float vx2, float vy2, float px, float py) {
        //e1 = (v1, v2) and e2 = (p, v1)
        //If p' matches with v1 (this means both have the same location), then
        //DP(e1, e2) = 0. e1 is then perpendicular to e2.
        //On the other side, if p' matches with v2 (thus p' is the same as v2), then
        //DP(e1, e2) = DP(e1, e1) = e1.x^2 + e1.y^2.
        //If p' lies somewhere between v1 and v2, then for the dot-product of e1 and e2 applies:
        //0 <= DP(e1,e2) <= e1.x^2 + e1.y^2.

        Vector2 e1 = new Vector2(vx1 - vx2, vy1 - vy2);
        Vector2 e2 = new Vector2(px - vx1, py - vy1);
        double recArea = dotProduct(e1, e1);
        double val = dotProduct(e1, e2);
        if (val < 0)
            return -1;
        else if (val == 0)
            return 0;
        else if (val > 0 && val < recArea)
            return 1;
        else if (val == recArea)
            return 2;
        else
            return 3;
    }

    static public double dotProduct(Vector2 e1, Vector2 e2) {
        return e1.x * e2.x + e1.y * e2.y;
    }

    static public int getNextVertice(float[] vertices, int currIdx, float[] nextVertices) {
        int idx1, idx2, idx3, idx4;
        currIdx += 2;

        idx1 = currIdx;
        idx2 = currIdx + 1;
        if (currIdx >= vertices.length - 2) {
            idx3 = 0;
            idx4 = 1;
        } else {
            idx3 = currIdx + 2;
            idx4 = currIdx + 3;
        }
        nextVertices[0] = vertices[idx1];
        nextVertices[1] = vertices[idx2];
        nextVertices[2] = vertices[idx3];
        nextVertices[3] = vertices[idx4];
        return currIdx;
    }

    static public int getPreviousVertice(float[] vertices, int currIdx, float[] nextVertices) {
        int idx1, idx2, idx3, idx4;
        idx3 = currIdx;
        idx4 = currIdx + 1;
        if (currIdx < 2) {
            idx1 = vertices.length - 2;
            idx2 = vertices.length - 1;
        } else {
            idx1 = currIdx - 2;
            idx2 = currIdx - 1;

        }

        currIdx = idx1;

        nextVertices[0] = vertices[idx1];
        nextVertices[1] = vertices[idx2];
        nextVertices[2] = vertices[idx3];
        nextVertices[3] = vertices[idx4];
        return currIdx;
    }

    static public boolean overlaps(Polygon polygon, Circle circle) {
        float[] vertices = polygon.getTransformedVertices();
        Vector2 center = new Vector2(circle.x, circle.y);
        float squareRadius = circle.radius * circle.radius;
        boolean ovelaps = false;


        if (Intersector.isPointInPolygon(vertices, 0, vertices.length, center.x, center.y)) {
            ovelaps = true;
        } else {
            for (int i = 0; i < vertices.length; i += 2) {
                if (i == 0) {
                    if (Intersector.intersectSegmentCircle(new Vector2(vertices[vertices.length - 2], vertices[vertices.length - 1]), new Vector2(vertices[i], vertices[i + 1]), center, squareRadius)) {
                        ovelaps = true;
                    }
                } else {
                    if (Intersector.intersectSegmentCircle(new Vector2(vertices[i - 2], vertices[i - 1]), new Vector2(vertices[i], vertices[i + 1]), center, squareRadius)) {
                        ovelaps = true;
                    }
                }
            }
        }
        return ovelaps;
    }

    static public boolean overlaps(Polygon polygon, Rectangle rect) {
        Polygon rectPolygon = new Polygon(new float[]{0, 0, rect.getWidth(), 0, rect.getWidth(), rect.getHeight(), 0, rect.getHeight()});
        rectPolygon.setPosition(rect.x, rect.y);
        boolean ovelaps = false;

        if (intersectPolygons(polygon, rectPolygon)) {
            ovelaps = true;
        }
        return ovelaps;
    }

    static public String logShape(Shape aShape) {
        if (aShape == null)
            return "null";
        String logStr = "";
        if (aShape.getType() == Shape.Type.POLYGON) {
            float[] vertices = ((PolygonShape) aShape).getShape().getTransformedVertices();
            logStr = "[";
            for (int i = 0; i <= vertices.length - 2; i += 2) {
                logStr += "(" + vertices[i] + "," + vertices[i + 1] + ") ";
            }
            logStr += "]";
        } else if (aShape.getType() == Shape.Type.CIRCLE) {
            logStr = "(X=" + ((CircleShape) aShape).getX() + ", Y=" + ((CircleShape) aShape).getY() + ", R=" + ((CircleShape) aShape).getShape().radius + ")";
        } else if (aShape.getType() == Shape.Type.RECT) {
            Rectangle rect = aShape.getBounds();
            logStr = "(" + rect.x + ", " + rect.y + ") (" + (rect.x + rect.getWidth()) + ", " + rect.y + ") (" + (rect.x + rect.getWidth()) + ", " + (rect.y + rect.getHeight()) + ") (" + rect.x + ", " + (rect.y + rect.getHeight()) + ")";
        }
        return logStr;
    }

    /**
     * Returns true if the specified point is in the polygon.
     *
     * @param offset Starting polygon index.
     * @param count  Number of array indices to use after offset.
     */
    public static boolean isPointInPolygon(float[] polygon, int offset, int count, float x, float y) {
        boolean oddNodes = false;
        int j = offset + count - 2;
        for (int i = offset, n = j; i <= n; i += 2) {
            float yi = polygon[i + 1];
            float yj = polygon[j + 1];
            if ((yi < y && yj > y) || (yj < y && yi > y)) {
                float xi = polygon[i];
                if (xi + (y - yi) / (yj - yi) * (polygon[j] - xi) < x) oddNodes = !oddNodes;
            }
            j = i;
        }
        return oddNodes;
    }

    /**
     * Intersects the two closed polygons and returns the polygon resulting from the intersection.
     * Follows the Sutherland-Hodgman algorithm.
     *
     * @param p1 The polygon that is being clipped
     * @param p2 The clip polygon
     * @return Whether the two polygons intersect.
     */
    public static boolean intersectPolygons(Polygon p1, Polygon p2) {
        //Convert polygons into more practical format
        ArrayList<Vector2> p1Points = new ArrayList<Vector2>();
        ArrayList<Vector2> p2Points = new ArrayList<Vector2>();
        float[] vertices1, vertices2;
        vertices1 = p1.getTransformedVertices();
        vertices2 = p2.getTransformedVertices();

        Vector2 pt1 = new Vector2();
        Vector2 pt2 = new Vector2();
        for (int i = 0; i < vertices1.length; i += 2) {
            if (i + 3 >= vertices1.length) {
                pt1.set(vertices1[i], vertices1[i + 1]);
                pt2.set(vertices1[0], vertices1[1]);
            } else {
                pt1.set(vertices1[i], vertices1[i + 1]);
                pt2.set(vertices1[i + 2], vertices1[i + 3]);
            }

            if (Intersector.intersectSegmentPolygon(pt1, pt2, p2)) {
                return true;
            }
        }
        for (int i = 0; i < vertices2.length; i += 2) {
            if (i + 3 >= vertices2.length) {
                pt1.set(vertices2[i], vertices2[i + 1]);
                pt2.set(vertices2[0], vertices2[1]);
            } else {
                pt1.set(vertices2[i], vertices2[i + 1]);
                pt2.set(vertices2[i + 2], vertices2[i + 3]);
            }

            if (Intersector.intersectSegmentPolygon(pt1, pt2, p1)) {
                return true;
            }
        }
        for (int i = 0; i < vertices1.length; i += 2) {
            if (isPointInPolygon(vertices2, 0, vertices2.length, vertices1[i], vertices1[i + 1])) {
                return true;
            }
        }
        for (int i = 0; i < vertices2.length; i += 2) {
            if (isPointInPolygon(vertices1, 0, vertices1.length, vertices2[i], vertices2[i + 1])) {
                return true;
            }
        }

        return false;

    }

    static public boolean segmentIntersectShape(Vector2 p1, Vector2 p2, Shape aShape) {
        if (aShape.getType() == Shape.Type.POLYGON) {
            Polygon poly = ((PolygonShape) aShape).getShape();
            float[] vertices = poly.getTransformedVertices();
            return isPointInPolygon(vertices, 0, vertices.length, p1.x, p1.y) ||
                    isPointInPolygon(vertices, 0, vertices.length, p2.x, p2.y) ||
                    Intersector.intersectSegmentPolygon(p1, p2, ((PolygonShape) aShape).getShape());
        } else if (aShape.getType() == Shape.Type.CIRCLE) {
            Circle circle = ((CircleShape) aShape).getShape();
            Vector2 center = new Vector2(circle.x, circle.y);
            return Intersector.intersectSegmentCircle(p1, p2, center, circle.radius);
        } else if (aShape.getType() == Shape.Type.RECT) {
            Rectangle rect = ((RectangleShape) aShape).getShape();
            Polygon rectPolygon = new Polygon(new float[]{0, 0, rect.getWidth(), 0, rect.getWidth(), rect.getHeight(), 0, rect.getHeight()});
            rectPolygon.setPosition(rect.x, rect.y);
            return Intersector.intersectSegmentPolygon(p1, p2, rectPolygon);

        }

        return false;
    }


    static public boolean overlaps(Shape aShape, Shape aOtherShape) {

        if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (intersectPolygons(((PolygonShape) aShape).getShape(), ((PolygonShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (ShapeUtils.overlaps(((PolygonShape) aShape).getShape(), ((CircleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.RECT) {
            if (ShapeUtils.overlaps(((PolygonShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (ShapeUtils.overlaps(((PolygonShape) aOtherShape).getShape(), ((CircleShape) aShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (Intersector.overlaps(((CircleShape) aShape).getShape(), ((CircleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.RECT) {
            if (Intersector.overlaps(((CircleShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (ShapeUtils.overlaps(((PolygonShape) aOtherShape).getShape(), ((RectangleShape) aShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (Intersector.overlaps(((CircleShape) aOtherShape).getShape(), ((RectangleShape) aShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.RECT) {
            if (Intersector.overlaps(((RectangleShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape()) ||
                    Intersector.overlaps(((RectangleShape) aOtherShape).getShape(), ((RectangleShape) aShape).getShape())) {
                return true;
            }
        }
        return false;
    }

    static public boolean overlaps(Shape aShape, Shape aOtherShape, Vector2 aTranslation) {

        if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (intersectPolygons(((PolygonShape) aShape).getShape(), ((PolygonShape) aOtherShape).getShape())) {
                return true;
            } else {
                Polygon poly = ((PolygonShape) aShape).getShape();
                float[] vertices = poly.getTransformedVertices();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();
                for (int i = 0; i < vertices.length - 2; i += 2) {
                    p1.set(vertices[i], vertices[i + 1]);
                    p2.set(vertices[i] + aTranslation.x, vertices[i + 1] + aTranslation.y);
                }
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
            return false;
        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (ShapeUtils.overlaps(((PolygonShape) aShape).getShape(), ((CircleShape) aOtherShape).getShape())) {
                return true;
            } else {
                Polygon poly = ((PolygonShape) aShape).getShape();
                float[] vertices = poly.getTransformedVertices();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();
                for (int i = 0; i < vertices.length - 2; i += 2) {
                    p1.set(vertices[i], vertices[i + 1]);
                    p2.set(vertices[i] + aTranslation.x, vertices[i + 1] + aTranslation.y);
                }
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.RECT) {
            if (ShapeUtils.overlaps(((PolygonShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape())) {
                return true;
            } else {
                Polygon poly = ((PolygonShape) aShape).getShape();
                float[] vertices = poly.getTransformedVertices();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();
                for (int i = 0; i < vertices.length - 2; i += 2) {
                    p1.set(vertices[i], vertices[i + 1]);
                    p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                }
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
            return false;
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (ShapeUtils.overlaps(((PolygonShape) aOtherShape).getShape(), ((CircleShape) aShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (Intersector.overlaps(((CircleShape) aShape).getShape(), ((CircleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.RECT) {
            if (Intersector.overlaps(((CircleShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape())) {
                return true;
            }
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.POLYGON) {
            if (ShapeUtils.overlaps(((PolygonShape) aOtherShape).getShape(), ((RectangleShape) aShape).getShape())) {
                return true;
            } else {
                Rectangle rect = ((RectangleShape) aShape).getShape();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();

                p1.set(rect.getX(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
            return false;
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.CIRCLE) {
            if (Intersector.overlaps(((CircleShape) aOtherShape).getShape(), ((RectangleShape) aShape).getShape())) {
                return true;
            } else {
                Rectangle rect = ((RectangleShape) aShape).getShape();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();

                p1.set(rect.getX(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
            return false;
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.RECT) {
            if (Intersector.overlaps(((RectangleShape) aShape).getShape(), ((RectangleShape) aOtherShape).getShape())) {
                return true;
            } else {
                Rectangle rect = ((RectangleShape) aShape).getShape();
                Vector2 p1 = new Vector2();
                Vector2 p2 = new Vector2();

                p1.set(rect.getX(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY() + rect.getHeight());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
                p1.set(rect.getX() + rect.getWidth(), rect.getY());
                p2.set(p1.x + aTranslation.x, p1.y + aTranslation.y);
                if (segmentIntersectShape(p1, p2, aOtherShape)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    static public float[] circleToPolygon(float x, float y, float radius, int segments) {
        if (segments <= 0) throw new IllegalArgumentException("segments must be > 0.");
        float angle = 2 * MathUtils.PI / segments;
        float cos = MathUtils.cos(angle);
        float sin = MathUtils.sin(angle);
        float cx = radius, cy = 0;
        float[] result = new float[segments * 2];

        for (int i = 0; i < segments * 2; i += 2) {
            result[i] = x + cx;
            result[i + 1] = y + cy;
            float temp = cx;
            cx = cos * cx - sin * cy;
            cy = sin * temp + cos * cy;
        }
        return result;
    }

    static public boolean overlaps(Shape aShape, Shape aOtherShape, Intersector.MinimumTranslationVector mvt) {

        if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.POLYGON) {
            return Intersector.overlapConvexPolygons(((PolygonShape) aShape).getShape(), ((PolygonShape) aOtherShape).getShape(),
                    mvt);

        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.CIRCLE) {
            CircleShape shape = ((CircleShape) aOtherShape);
            float[] circlePoly = circleToPolygon(shape.getX(), shape.getY(), shape.getShape().radius, 16);
            return Intersector.overlapConvexPolygons(((PolygonShape) aShape).getShape().getTransformedVertices(), circlePoly,
                    mvt);
        } else if (aShape.getType() == Shape.Type.POLYGON && aOtherShape.getType() == Shape.Type.RECT) {
            RectangleShape shape = ((RectangleShape) aOtherShape);

            Polygon polygon = new Polygon(new float[]{0, 0, shape.getShape().getWidth(), 0, shape.getShape().getWidth(), shape.getShape().getHeight(), 0, shape.getShape().getHeight()});
            polygon.setPosition(shape.getX(), shape.getY());
            return Intersector.overlapConvexPolygons(((PolygonShape) aShape).getShape(), polygon,
                    mvt);
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.POLYGON) {
            CircleShape shape = ((CircleShape) aShape);
            float[] circlePoly = circleToPolygon(shape.getX(), shape.getY(), shape.getShape().radius, 16);
            return Intersector.overlapConvexPolygons(circlePoly, ((PolygonShape) aOtherShape).getShape().getTransformedVertices(),
                    mvt);
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.CIRCLE) {
            CircleShape shape = ((CircleShape) aShape);
            float[] circlePoly = circleToPolygon(shape.getX(), shape.getY(), shape.getShape().radius, 16);
            CircleShape othershape = ((CircleShape) aOtherShape);
            float[] otherCirclePoly = circleToPolygon(othershape.getX(), othershape.getY(), othershape.getShape().radius, 16);
            return Intersector.overlapConvexPolygons(circlePoly, otherCirclePoly,
                    mvt);
        } else if (aShape.getType() == Shape.Type.CIRCLE && aOtherShape.getType() == Shape.Type.RECT) {
            CircleShape shape = ((CircleShape) aShape);
            float[] circlePoly = circleToPolygon(shape.getX(), shape.getY(), shape.getShape().radius, 16);
            RectangleShape otherShape = ((RectangleShape) aOtherShape);
            Polygon polygon = new Polygon(new float[]{0, 0, otherShape.getShape().getWidth(), 0, otherShape.getShape().getWidth(), otherShape.getShape().getHeight(), 0, otherShape.getShape().getHeight()});
            polygon.setPosition(otherShape.getX(), otherShape.getY());
            return Intersector.overlapConvexPolygons(circlePoly, ((PolygonShape) aOtherShape).getShape().getTransformedVertices(),
                    mvt);
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.POLYGON) {
            RectangleShape shape = ((RectangleShape) aShape);
            Polygon polygon = new Polygon(new float[]{0, 0, shape.getShape().getWidth(), 0, shape.getShape().getWidth(), shape.getShape().getHeight(), 0, shape.getShape().getHeight()});
            polygon.setPosition(shape.getX(), shape.getY());
            return Intersector.overlapConvexPolygons(polygon.getTransformedVertices(), ((PolygonShape) aOtherShape).getShape().getTransformedVertices(),
                    mvt);
        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.CIRCLE) {
            RectangleShape shape = ((RectangleShape) aShape);
            Polygon polygon = new Polygon(new float[]{0, 0, shape.getShape().getWidth(), 0, shape.getShape().getWidth(), shape.getShape().getHeight(), 0, shape.getShape().getHeight()});
            polygon.setPosition(shape.getX(), shape.getY());
            CircleShape othershape = ((CircleShape) aOtherShape);
            float[] otherCirclePoly = circleToPolygon(othershape.getX(), othershape.getY(), othershape.getShape().radius, 16);
            return Intersector.overlapConvexPolygons(polygon.getTransformedVertices(), otherCirclePoly,
                    mvt);

        } else if (aShape.getType() == Shape.Type.RECT && aOtherShape.getType() == Shape.Type.RECT) {
            RectangleShape shape = ((RectangleShape) aShape);
            Polygon polygon = new Polygon(new float[]{0, 0, shape.getShape().getWidth(), 0, shape.getShape().getWidth(), shape.getShape().getHeight(), 0, shape.getShape().getHeight()});
            polygon.setPosition(shape.getX(), shape.getY());
            RectangleShape othershape = ((RectangleShape) aOtherShape);
            Polygon otherpolygon = new Polygon(new float[]{0, 0, othershape.getShape().getWidth(), 0, othershape.getShape().getWidth(), othershape.getShape().getHeight(), 0, othershape.getShape().getHeight()});
            otherpolygon.setPosition(othershape.getX(), othershape.getY());
            return Intersector.overlapConvexPolygons(polygon.getTransformedVertices(), otherpolygon.getTransformedVertices(),
                    mvt);
        }
        return false;
    }

    /********************************************/

    // calculate the projection range of a polygon along an axis
    static void getInterval(final float[] axVertices, final Vector2 xAxis, MutableFloat min, MutableFloat max) {
        Vector2 vertice = new Vector2();
        vertice.set(axVertices[0], axVertices[1]);
        min.value = max.value = (vertice.dot(xAxis));

        for (int i = 1; i < axVertices.length - 2; i++) {
            vertice.set(axVertices[i], axVertices[i + 1]);
            float d = (vertice.dot(xAxis));
            if (d < min.value) min.value = d;
            else if (d > max.value) max.value = d;
        }
    }

    static boolean intervalIntersect(final float[] verticesA,
                                     final float[] verticesB,
                                     final Vector2 xAxis,
                                     final Vector2 xOffset, final Vector2 xVel,
                                     MutableFloat taxis, float tmax) {
        MutableFloat min0 = new MutableFloat(0);
        MutableFloat max0 = new MutableFloat(0);
        MutableFloat min1 = new MutableFloat(0);
        MutableFloat max1 = new MutableFloat(0);
        getInterval(verticesA, xAxis, min0, max0);
        getInterval(verticesB, xAxis, min1, max1);

        float h = xOffset.dot(xAxis);
        min0.value += h;
        max0.value += h;

        float d0 = min0.value - max1.value; // if overlapped, do < 0
        float d1 = min1.value - max0.value; // if overlapped, d1 > 0

        // separated, test dynamic intervals
        if (d0 > 0.0f || d1 > 0.0f) {
            float v = xVel.dot(xAxis);

            // small velocity, so only the overlap test will be relevant.
            if (Math.abs(v) < 0.0000001f)
                return false;

            float t0 = -d0 / v; // time of impact to d0 reaches 0
            float t1 = d1 / v; // time of impact to d0 reaches 1

            if (t0 > t1) {
                float temp = t0;
                t0 = t1;
                t1 = temp;
            }
            taxis.value = (t0 > 0.0f) ? t0 : t1;

            if (taxis.value < 0.0f || taxis.value > tmax)
                return false;

            return true;
        } else {
            // overlap. get the interval, as a the smallest of |d0| and |d1|
            // return negative number to mark it as an overlap
            taxis.value = (d0 > d1) ? d0 : d1;
            return true;
        }
    }


    public static boolean poygoneCollide(final Shape shapeA,
                                         final Shape shapeB,
                                         Vector2 xVel,
                                         Vector2 N, MutableFloat t) {
        float[] verticesA = null;
        float[] verticesB = null;

        if (shapeA.getType() == Shape.Type.RECT) {
            Rectangle rect = ((RectangleShape) shapeA).getShape();
            Polygon rectPolygon = new Polygon(new float[]{0, 0, rect.getWidth(), 0, rect.getWidth(), rect.getHeight(), 0, rect.getHeight()});
            rectPolygon.setPosition(rect.x, rect.y);
            verticesA = rectPolygon.getTransformedVertices();
        } else if (shapeA.getType() == Shape.Type.POLYGON) {
            verticesA = ((PolygonShape) shapeA).getShape().getTransformedVertices();
        }
        if (shapeB.getType() == Shape.Type.RECT) {
            Rectangle rect = ((RectangleShape) shapeB).getShape();
            Polygon rectPolygon = new Polygon(new float[]{0, 0, rect.getWidth(), 0, rect.getWidth(), rect.getHeight(), 0, rect.getHeight()});
            rectPolygon.setPosition(rect.x, rect.y);
            verticesB = rectPolygon.getTransformedVertices();
        } else if (shapeB.getType() == Shape.Type.POLYGON) {
            verticesB = ((PolygonShape) shapeB).getShape().getTransformedVertices();
        }

        Vector2 xOffset = new Vector2();
        xOffset.set(shapeA.getX() - shapeB.getX(), shapeA.getY() - shapeB.getY());

        return poygoneCollide(verticesA, verticesB, xOffset, xVel, N, t);


    }

    static boolean poygoneCollide(final float[] verticesA,
                                  final float[] verticesB,
                                  Vector2 xOffset, Vector2 xVel,
                                  Vector2 N, MutableFloat t) {
        if (verticesA == null || verticesB == null) return false;

        // All the separation axes
        int nbAxis = verticesA.length / 2 + verticesB.length / 2;
        float fVel2 = xVel.len2();

        if (fVel2 > 0.00001f) {
            nbAxis++;
        }
        Vector2 xAxis[] = new Vector2[nbAxis];
        MutableFloat taxis[] = new MutableFloat[nbAxis];
        for (int i = 0; i < taxis.length; i++) {
            taxis[i] = new MutableFloat(0);
        }
        int iNumAxes = 0;

        xAxis[iNumAxes] = new Vector2(-xVel.y, xVel.x);

        if (fVel2 > 0.00001f) {

            if (!intervalIntersect(verticesA,
                    verticesB,
                    xAxis[iNumAxes],
                    xOffset, xVel,
                    taxis[iNumAxes], t.value)) {
                return false;
            }
            iNumAxes++;
        }

        // test separation axes of A
        Vector2 E0 = null;
        Vector2 E1 = null;
        Vector2 E = null;
        Vector2 verticej = new Vector2();
        Vector2 verticei = new Vector2();
        for (int j = verticesA.length - 2, i = 0; i < verticesA.length - 2; j = i, i = i + 2) {
            verticej.set(verticesA[j], verticesA[j + 1]);
            verticei.set(verticesA[i], verticesA[i + 1]);
            E0 = verticej;
            E1 = verticei;
            E = E1.sub(E0);
            xAxis[iNumAxes] = new Vector2(-E.y, E.x);

            if (!intervalIntersect(verticesA,
                    verticesB,
                    xAxis[iNumAxes],
                    xOffset, xVel,
                    taxis[iNumAxes], t.value)) {
                return false;
            }

            iNumAxes++;
        }

        // test separation axes of B
        for (int j = verticesB.length - 2, i = 0; i < verticesB.length - 2; j = i, i = i + 2) {
            verticej.set(verticesB[j], verticesB[j + 1]);
            verticei.set(verticesB[i], verticesB[i + 1]);
            E0 = verticej;
            E1 = verticei;
            E = E1.sub(E0);
            xAxis[iNumAxes] = new Vector2(-E.y, E.x);

            if (!intervalIntersect(verticesA,
                    verticesB,
                    xAxis[iNumAxes],
                    xOffset, xVel,
                    taxis[iNumAxes], t.value)) {
                return false;
            }

            iNumAxes++;
        }

        if (!findMTD(xAxis, taxis, iNumAxes, N, t))
            return false;

        // make sure the polygons gets pushed away from each other.
        if (N.dot(xOffset) < 0.0f)
            N.set(-N.x, -N.y);

        return true;
    }

    static boolean findMTD(Vector2[] xAxis, MutableFloat[] taxis, int iNumAxes, Vector2 N, MutableFloat t) {
        // find collision first
        int mini = -1;
        t.value = 0.0f;
        for (int i = 0; i < iNumAxes; i++) {
            if (taxis[i].value > 0) {
                if (taxis[i].value > t.value) {
                    mini = i;
                    t.value = taxis[i].value;
                    N.set(xAxis[i].x, xAxis[i].y);
                    N.nor();
                }
            }
        }

        // found one
        if (mini != -1)
            return true;

        // nope, find overlaps
        mini = -1;
        for (int i = 0; i < iNumAxes; i++) {
            float n = xAxis[i].len();
            xAxis[i].nor();
            taxis[i].value /= n;

            if (taxis[i].value > t.value || mini == -1) {
                mini = i;
                t.value = taxis[i].value;
                N.set(xAxis[i].x, xAxis[i].y);
            }
        }

      /*  if (mini == -1)
            printf("Error\n");*/

        return (mini != -1);
    }

    public static class MutableFloat {
        public MutableFloat(float initValue) {
            value = initValue;
        }

        public float value;
    }


    public static boolean checkMove(Entity[] aEntities, Shape aShapeToTest, Vector2 aTargetPos, Shape aShapeToIgnore, Vector2 aPosFinale, int maxDepth) {
        Intersector.MinimumTranslationVector mvt = new Intersector.MinimumTranslationVector();
        Vector2 posOrigin = new Vector2(aShapeToTest.getX(), aShapeToTest.getY());
        aShapeToTest.setX(aTargetPos.x);
        aShapeToTest.setY(aTargetPos.y);
        if (aEntities != null && aEntities.length > 0) {
            for (int i = 0; i < aEntities.length; i++) {

                CollisionObstacleComponent collision = aEntities[i].getComponent(CollisionObstacleComponent.class);
                if (collision == null || collision.mShape == aShapeToIgnore || collision.mShape == aShapeToTest)
                    continue;

                if ((collision.mType & CollisionObstacleComponent.OBSTACLE) != 0 || ((collision.mType & CollisionObstacleComponent.MAPINTERACTION) != 0)) {
                    if (collision.mShape.getBounds().overlaps(aShapeToTest.getBounds()) || aShapeToTest.getBounds().overlaps(collision.mShape.getBounds())) {
                        if (ShapeUtils.overlaps(aShapeToTest, collision.mShape, mvt)) {
                            mvt.normal.setLength((float) Math.max(mvt.depth * 1.1, Math.sqrt(PathMap.CHECK_RADIUS)));
                            aPosFinale.set(aShapeToTest.getX() + mvt.normal.x, aShapeToTest.getY() + mvt.normal.y);

                            //Gdx.app.debug("DEBUG", "checkMove overlaps "+logShape(collision.mShape)+" mvt=(" + mvt.normal.x + "," + mvt.normal.y + ")");

                            if (!ShapeUtils.segmentIntersectShape(posOrigin, aPosFinale, collision.mShape)) {
                                //Gdx.app.debug("DEBUG", "posFinal intersects Shape aPosFinale=(" + aPosFinale.x + "," + aPosFinale.y + ")");

                                if (maxDepth > 0) {
                                    //Gdx.app.debug("DEBUG", "maxDepth=" +maxDepth);

                                    float targetPosX = aTargetPos.x;
                                    float targetPosY = aTargetPos.y;
                                    aTargetPos.set(aShapeToTest.getX() + mvt.normal.x,
                                            aShapeToTest.getY() + mvt.normal.y);
                                    if (checkMove(aEntities, aShapeToTest, aTargetPos, collision.mShape, aPosFinale, maxDepth - 1)) {
                                        aShapeToTest.setX(posOrigin.x);
                                        aShapeToTest.setY(posOrigin.y);
                                        // aPosFinale.set(aTargetPos);
                                        return true;
                                    }
                                    aTargetPos.set(targetPosX, targetPosY);
                                }


                            }
                        }
                    }

                    aPosFinale.set(aShapeToTest.getX(), aShapeToTest.getY());
                    if (ShapeUtils.segmentIntersectShape(posOrigin, aPosFinale, collision.mShape)) {
                        aShapeToTest.setX(posOrigin.x);
                        aShapeToTest.setY(posOrigin.y);
                        aPosFinale.set(posOrigin);
                        return false;
                    }


                }


            }

        }
        aShapeToTest.setX(posOrigin.x);
        aShapeToTest.setY(posOrigin.y);
        aPosFinale.set(aTargetPos);
        return true;
    }

}
