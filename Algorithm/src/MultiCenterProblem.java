import java.util.*;

// 정점 클래스
class Point { // 2차원 평면 상에서 x y좌표
    int x, y, id;

    public Point(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }
}

// 간선 클래스
class Edge implements Comparable<Edge> { // Comparble 은 인터페이스로 구현하여 가중치 기준 정렬을 위함
    Point u, v;
    double weight; //간선 가중치

    public Edge(Point u, Point v) {
        this.u = u;
        this.v = v;
        this.weight = Math.sqrt(Math.pow(u.x - v.x, 2) + Math.pow(u.y - v.y, 2));
    }

    @Override
    public int compareTo(Edge edge) {
        return Double.compare(this.weight, edge.weight);
    }
}

// Disjoint Set 클래스
class DisjointSet { // 서로소 집합 클래스
    Map<Integer, Integer> parent;

    public DisjointSet() {
        this.parent = new HashMap<>();
    }

    public void makeSet(int x) { //자기 자신을 원소로하는 초기화과정
        parent.put(x, x);
    }

    public int find(int x) { // 대표원소 찾기
        if (parent.get(x) == x) {
            return x;
        } else {
            int root = find(parent.get(x));
            parent.put(x, root);
            return root;
        } //Path compression 적용 출처 https://yonghwankim-dev.tistory.com/236
    }

    public void union(int x, int y) { // 두 원소 합쳐버리깅
        int rootX = find(x);
        int rootY = find(y);
        if (rootX != rootY) {
            parent.put(rootX, rootY);
        }
    }
}

public class MultiCenterProblem {

    // Kruskal 알고리즘을 사용하여 Euclidean MST를 구함
    // points 리스트에 정점들 간의 모든 가능한 간선을 생성하고 가중치 순으로 정렬 -> FIND MST
    public static List<Edge> euclideanMST(List<Point> points) { //출처 https://chanhuiseok.github.io/posts/algo-33/    https://4legs-study.tistory.com/111
        List<Edge> edges = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                edges.add(new Edge(points.get(i), points.get(j)));
            }
        }

        edges.sort(Edge::compareTo);

        DisjointSet disjointSet = new DisjointSet();
        for (Point point : points) {
            disjointSet.makeSet(point.id);
        }

        List<Edge> mst = new ArrayList<>();
        for (Edge edge : edges) {
            int uRoot = disjointSet.find(edge.u.id);
            int vRoot = disjointSet.find(edge.v.id);
            if (uRoot != vRoot) {
                mst.add(edge);
                disjointSet.union(uRoot, vRoot);
            }
        }

        return mst;
    }

    public static Map<Integer, Point> findCenters(List<Point> points, List<Edge> mst, int c) { // 센터 찾는 알고리즘
        Map<Integer, Point> centers = new HashMap<>();

        // MST에서 c개의 센터 선택
        for (int i = 0; i < c; i++) { // 정렬되어있는 MST 에서 가중치가 작은 간선부터 순서대로 센터에 지정
            centers.put(i + 1, mst.get(i).u);
        }

        return centers;
    }

    // 각 정점에서 선택된 센터까지의 거리 계산
    public static Map<Integer, Map<Integer, Double>> calculateDistances(List<Point> points, Map<Integer, Point> centers) { //간단한 거리계산
        Map<Integer, Map<Integer, Double>> distances = new HashMap<>();

        for (Point point : points) {
            distances.put(point.id, new HashMap<>());

            for (Map.Entry<Integer, Point> entry : centers.entrySet()) {
                Point center = entry.getValue();
                double distance = Math.sqrt(Math.pow(point.x - center.x, 2) + Math.pow(point.y - center.y, 2));
                distances.get(point.id).put(entry.getKey(), distance);
            }
        }

        return distances;
    }


    //출력
    public static void printResults(Map<Integer, Point> centers, Map<Integer, Map<Integer, Double>> distances) {
        System.out.println("선택된 Centers:");
        for (Map.Entry<Integer, Point> entry : centers.entrySet()) {
            System.out.println("좌표 (" + entry.getValue().x + ", " + entry.getValue().y + ")");
        }

        System.out.println("\n각 점에 대한 정보 :");
        for (Map.Entry<Integer, Map<Integer, Double>> entry : distances.entrySet()) {
            int pointId = entry.getKey();
            Map<Integer, Double> centerDistances = entry.getValue();

            System.out.print("점 " + pointId + "은(는) ");
            boolean hasNearestCenter = false;
            double minDistance = Double.MAX_VALUE;
            int nearestCenter = -1;

            for (Map.Entry<Integer, Double> centerEntry : centerDistances.entrySet()) {
                int centerId = centerEntry.getKey();
                double distance = centerEntry.getValue();
                System.out.printf("센터 (%d, %d)와의 거리가 %.2f, ", centers.get(centerId).x, centers.get(centerId).y, distance);

                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCenter = centerId;
                    hasNearestCenter = true;
                }
            }

            if (hasNearestCenter) {
                System.out.println("가장 가까운 센터는 센터 " + nearestCenter + "입니다.");
            } else {
                System.out.println("가장 가까운 센터가 없습니다.");
            }
        }
    }
// 입력받는 메인
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("100x100 줄과 줄이 교차하는 지점에 정점들의 개수를 입력하세요: ");
        int n = scanner.nextInt();

        List<Point> points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            System.out.print("점 " + (i + 1) + "의 좌표를 입력하세요 (x y): ");
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            points.add(new Point(x, y, i + 1));
        }

        List<Edge> mst = euclideanMST(points);

        System.out.print("센터의 개수를 입력하세요 (c): ");
        int c = scanner.nextInt();

        Map<Integer, Point> centers = findCenters(points, mst, c);

        Map<Integer, Map<Integer, Double>> distances = calculateDistances(points, centers);

        // 결과 출력
        printResults(centers, distances);
    }
}
