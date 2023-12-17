import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class EscapeProblem {

    public static void main(String[] args) {
        System.out.println(" 디버깅 열 main strat ");
        Scanner s = new Scanner(System.in);
        System.out.print("그리드의 크기 입력 : ");
        int n = s.nextInt();
        // 정점 입력
        System.out.print("탈출을 위한 정점의 개수 입력: ");
        int r = s.nextInt();

        int[][] escapePoints = new int[r][2];

        for (int i = 0; i < r; i++) {
            System.out.print("정점 " + (i + 1) + "의 x 좌표 입력: ");
            escapePoints[i][0] = s.nextInt();

            System.out.print("정점 " + (i + 1) + "의 y 좌표 입력: ");
            escapePoints[i][1] = s.nextInt();
        }

        char[][] escapeGrid = escapePaths(n, escapePoints);
        printGrid("초기 Escape Grid:", escapeGrid);

        // 네트워크 플로우 문제로 변환
        int source = 0;
        int sink = n * n + 1;
        int[][] capacities = convertToCapacities(escapeGrid, n, escapePoints);
        int maxFlow = fordFulkerson(capacities, source, sink);
        System.out.println("Maximum Flow (Network Flow Problem): " + maxFlow);

        // 탈출 문제로 변환
        char[][] escapeGridAfterFlow = convertToEscapeGrid(capacities, escapeGrid, n);
        printGrid("탈출 문제로 변환된 Escape Grid:", escapeGridAfterFlow);
        System.out.println(" 디버깅 열 main ends ");
    }

    private static char[][] escapePaths(int n, int[][] escapePoints) {
        System.out.println(" 디버깅 열 escapePaths strat ");
        char[][] grid = new char[n][n];
        boolean[][] visited = new boolean[n][n];

        for (int[] point : escapePoints) {
            int x = point[0];
            int y = point[1];
            grid[y - 1][x - 1] = 'X'; // 탈출 지점 표시
        }

        for (int[] point : escapePoints) {
            bfs(grid, visited, n, point[0] - 1, point[1] - 1);
        }
        System.out.println(" 디버깅 열 escapePaths end ");
        return grid;
    }

    private static void bfs(char[][] grid, boolean[][] visited, int n, int startX, int startY) {
        System.out.println(" 디버깅 열 bfs strat ");
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startX, startY});

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int x = current[0];
            int y = current[1];

            if (x < 0 || x >= n || y < 0 || y >= n || grid[y][x] == 'X' || visited[y][x]) {
                continue;
            }

            grid[y][x] = '*'; // '*'는 탈출 경로를 나타내는 문자
            visited[y][x] = true;

            // Debugging
            System.out.println("After BFS:");
            printGrid("Escape Grid:", grid);

            // 초기화된 visited 배열로 BFS 수행
            visited = new boolean[n][n];

            queue.offer(new int[]{x + 1, y});
            queue.offer(new int[]{x - 1, y});
            queue.offer(new int[]{x, y + 1});
            queue.offer(new int[]{x, y - 1});
            System.out.println(" 디버깅 열 bfs end ");
        }
    }


    private static int[][] convertToCapacities(char[][] escapeGrid, int n, int[][] escapePoints) {
        System.out.println(" 디버깅 열 convertToCpacities strat ");
        int[][] capacities = new int[n * n + 2][n * n + 2];

        int source = 0;
        int sink = n * n + 1;

        // Source에서 첫 번째 행으로의 용량 설정
        for (int i = 1; i <= n; i++) {
            capacities[source][i] = 1;
        }

        // 각 탈출 지점에서 마지막 행으로의 용량 설정
        for (int[] point : escapePoints) {
            int x = point[0] - 1;
            int y = point[1] - 1;
            int node = y * n + x + 1;
            capacities[node][sink] = 1;
        }

        // '*'로 표시된 경로 사이의 용량 설정
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (escapeGrid[i][j] == '*') {
                    int node = i * n + j + 1;
                    int adjNode;

                    if (i - 1 >= 0 && escapeGrid[i - 1][j] == '*') {
                        adjNode = (i - 1) * n + j + 1;
                        capacities[node][adjNode] = 1;
                    }

                    if (i + 1 < n && escapeGrid[i + 1][j] == '*') {
                        adjNode = (i + 1) * n + j + 1;
                        capacities[node][adjNode] = 1;
                    }

                    if (j - 1 >= 0 && escapeGrid[i][j - 1] == '*') {
                        adjNode = i * n + j;
                        capacities[node][adjNode] = 1;
                    }

                    if (j + 1 < n && escapeGrid[i][j + 1] == '*') {
                        adjNode = i * n + j + 2;
                        capacities[node][adjNode] = 1;
                    }
                }
            }
        }
        System.out.println(" 디버깅 열 convertToCpacities end ");
        return capacities;
    }


    private static int fordFulkerson(int[][] capacities, int source, int sink) {
        System.out.println(" 디버깅 열 fordFulkerson strat ");
        int[][] residualGraph = new int[capacities.length][capacities[0].length];
        for (int i = 0; i < capacities.length; i++) {
            System.arraycopy(capacities[i], 0, residualGraph[i], 0, capacities[0].length);
        }

        int[] parent = new int[residualGraph.length];
        int maxFlow = 0;

        while (bfs(residualGraph, source, sink, parent)) {
            int pathFlow = Integer.MAX_VALUE;

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }

            for (int v = sink; v != source; v = parent[v]) {
                int u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }

            maxFlow += pathFlow;
        }
        System.out.println(" 디버깅 열 fordFulkerson end ");
        return maxFlow;
    }

    private static boolean bfs(int[][] graph, int source, int sink, int[] parent) {
        System.out.println(" 디버깅 열 bfs SINK strat ");
        boolean[] visited = new boolean[graph.length];
        Queue<Integer> queue = new LinkedList<>();
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (!queue.isEmpty()) {
            int u = queue.poll();

            for (int v = 0; v < graph.length; v++) {
                if (!visited[v] && graph[u][v] > 0) {
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;

                    if (v == sink) {
                        return true;
                    }
                }
            }
        }
        System.out.println(" 디버깅 열 bfs SINK end ");
        return false;
    }

    private static char[][] convertToEscapeGrid(int[][] capacities, char[][] escapeGrid, int n) {
        System.out.println(" 디버깅 열 convertToEscapeGrid start ");
        char[][] resultGrid = new char[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                resultGrid[i][j] = escapeGrid[i][j];
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (escapeGrid[i][j] == ' ' && capacities[i * n + j + 1][0] == 0) {
                    resultGrid[i][j] = '*';
                }
            }
        }
        System.out.println(" 디버깅 열 convertToEscapeGrid end ");
        return resultGrid;
    }

    private static void printGrid(String title, char[][] grid) {
        System.out.println(" 디버깅 열 printGrid start ");
        System.out.println(title);
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell + " ");
            }
            System.out.println();
        }
        System.out.println(" 디버깅 열 printGrid end ");
        System.out.println();
    }
}
