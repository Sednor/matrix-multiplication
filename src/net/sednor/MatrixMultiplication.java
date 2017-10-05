package net.sednor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by illia.
 */
public class MatrixMultiplication {
    static final int THREAD_COUNT = 4;
    private static final int[][] MATRIX_A = new int[][]
            {
                    { 5, 6, 2, 5, 6, 2 },
                    { 7, 8, 9, 5, 6, 2 }
            };
    private static final int[][] MATRIX_B = new int[][]
            {
                    { 1, 2 },
                    { 3, 4 },
                    { 3, 4 },
                    { 3, 4 },
                    { 3, 4 },
                    { 3, 4 }
            };

    public static void main(String arg[]) throws Exception {
        System.out.println(new Matrix(MATRIX_A).multiply(new Matrix(MATRIX_B)));
    }

    static class Matrix {
        private int rowCount, columnCount, data[][];

        Matrix(int data[][]) {
            this.data = data;
            this.rowCount = data.length;
            this.columnCount = data[0].length;

        }

        Matrix multiply(Matrix matrix) {
            if (this.columnCount != matrix.rowCount) {
                throw new RuntimeException("It's not a square matrix!");
            }
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
            try {
                List<Future<Integer>> result = executor.invokeAll(Matrix.getTasks(this, matrix));
                int matrixResult[][] = new int[this.rowCount][matrix.columnCount];
                for (int i = 0; i < this.rowCount; ++i) {
                    for (int j = 0; j < matrix.columnCount; ++j) {
                        matrixResult[i][j] = result.get(i * matrix.columnCount + j).get();
                    }
                }
                return new Matrix(matrixResult);
            } catch (Exception e) {
                executor.shutdown();
            }
            return null;
        }

        static List<Callable<Integer>> getTasks(Matrix matrixA, Matrix matrixB) {
            List<Callable<Integer>> items = new ArrayList<>();
            for (int i = 0; i < matrixA.rowCount; ++i) {
                for (int j = 0; j < matrixB.columnCount; ++j) {
                    items.add(getTask(matrixA, matrixB, i, j));
                }
            }
            return items;
        }

        private static Callable<Integer> getTask(Matrix matrixA, Matrix matrixB, int rowId, int colId) {
            return () -> {
                String message = String.format("A[%s] * B[%s]", rowId, colId);
                System.out.println("Thread id: " + Thread.currentThread().getId() + " :Started  Task. " + message);
                int result = 0;
                for (int i = 0; i < matrixA.columnCount; ++i) {
                    result = result + matrixA.data[rowId][i] * matrixB.data[i][colId];
                }
                System.out.println("Thread id: " + Thread.currentThread().getId() + " :Finished Task. " + message + " = " + result);
                return result;
            };
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (int[] item: data) {
                builder.append(Arrays.toString(item)).append("\n");
            }
            return builder.toString();
        }
    }
}
