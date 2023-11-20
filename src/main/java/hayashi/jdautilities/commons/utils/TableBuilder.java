// Copyright 2023 Aidan Lim (southernscreamer32) <aidanlim192@gmail.com>
// Copyright 2016-2018 John Grosh (jagrosh) <john.a.grosh@gmail.com> & Kaidan Gustave (TheMonitorLizard).
//
// This file is part of Raiko.
//
// Raiko is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Raiko is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with Raiko. If not, see <http://www.gnu.org/licenses/>.


package hayashi.jdautilities.commons.utils;

import net.dv8tion.jda.internal.utils.Checks;

import java.util.Arrays;
import java.util.Objects;

public class TableBuilder {

    private String[][] values;
    private String[] headers, rowNames;
    private Borders borders;
    private String tableName = "";
    private Alignment alignment = Alignment.CENTER;
    private int padding = 1;
    private boolean codeblock, frame;
    private boolean autoAdjust = true;

    public String build() {
        Checks.notNull(borders, "Borders");
        Checks.notNull(values, "Values");
        Checks.notEmpty(values, "Values");
        Checks.check(Arrays.stream(values).allMatch((row) -> Arrays.stream(row).allMatch(Objects::nonNull)), "A value may not be null");
        Checks.check(padding >= 0, "Padding must not be < 0");

        boolean headersPresent = headers != null;
        boolean rowNamesPresent = rowNames != null;

        int rows = (rowNamesPresent ? rowNames.length : values.length);
        int columns = (headersPresent ? headers.length : Arrays.stream(values).mapToInt((s) -> s.length).max().orElse(0));

        Checks.check(values.length >= rows, "The amount of rows must not be smaller than specified in the row names");
        int oldColumns = columns;
        Checks.check(Arrays.stream(values).noneMatch((row) -> row.length < oldColumns), "The amount of columns must be consistent");

        if (headersPresent)
            rows++;
        if (rowNamesPresent)
            columns++;

        // insert headers and row names (if present)
        String[][] newValues = new String[rows][columns];
        if (headersPresent && rowNamesPresent) {
            newValues[0][0] = tableName;
            System.arraycopy(headers, 0, newValues[0], 1, headers.length);
            for (int i = 1; i < rows; i++) {
                newValues[i][0] = rowNames[i - 1];
                System.arraycopy(values[i - 1], 0, newValues[i], 1, columns - 1);
            }
        } else if (rowNamesPresent) {
            for (int i = 0; i < rows; i++) {
                newValues[i][0] = rowNames[i];
                System.arraycopy(values[i], 0, newValues[i], 1, columns - 1);
            }
        } else if (headersPresent) {
            System.arraycopy(headers, 0, newValues[0], 0, columns);
            System.arraycopy(values, 0, newValues, 1, rows - 1);
        } else {
            newValues = values;
        }

        values = newValues;

        if (autoAdjust) {
            // find the max. value for each column
            int[] maxLengths = new int[columns];

            for (String[] row : values) {
                for (int i = 0; i < row.length; i++) {
                    int length = row[i].length();
                    if (length > maxLengths[i])
                        maxLengths[i] = length;
                }
            }

            // align values
            for (String[] row : values) {
                for (int j = 0; j < row.length; j++) {
                    String value = row[j];
                    int adjustment = maxLengths[j] - value.length();
                    StringBuilder newValue = new StringBuilder();
                    setAlignment(adjustment, value, newValue);
                    row[j] = newValue.toString();
                }
            }
        } else {
            Checks.notNull(headers, "Headers");
            boolean check = true;
            out:
            for (String[] row : values) {
                for (int i = 0; i < headers.length; i++) {
                    if (row[i].length() > headers[i].length()) {
                        check = false;
                        break out;
                    }
                }
            }
            Checks.check(check, "Length of values must not be longer than length of headers");
        }

        StringBuilder builder = new StringBuilder();

        if (codeblock)
            builder.append("```\n");

        String[] firstRow = values[0];

        // outline
        if (frame) {
            builder.append(borders.upLeftCorner);
            for (int j = 0; j < firstRow.length; j++) {
                builder.append(String.valueOf(borders.horizontalOutline).repeat(firstRow[j].length()));

                if (j == 0)
                    builder.append(borders.firstColumnUpperIntersection);
                else if (j < firstRow.length - 1)
                    builder.append(borders.upperIntersection);
            }
            builder.append(borders.upRightCorner).append("\n");
        }

        appendRow(builder, firstRow); // header

        builder.append("\n");

        // header delimiter
        if (frame)
            builder.append(borders.headerLeftIntersection);

        for (int i = 0; i < firstRow.length; i++) {
            builder.append(String.valueOf(borders.headerDelimiter).repeat(firstRow[i].length()));

            if (i == 0)
                builder.append(borders.headerColumnCrossDelimiter);
            else if (i < firstRow.length - 1)
                builder.append(borders.headerCrossDelimiter);

        }

        if (frame)
            builder.append(borders.headerRightIntersection);

        builder.append("\n");


        // append row after row
        for (int i = 1; i < rows; i++) {
            String[] row = values[i];

            appendRow(builder, row);

            // delimiter
            if (i < values.length - 1) {
                builder.append("\n");

                if (frame)
                    builder.append(borders.leftIntersection);

                for (int j = 0; j < row.length; j++) {
                    builder.append(String.valueOf(borders.rowDelimiter).repeat(row[j].length()));

                    if (j == 0)
                        builder.append(borders.firstColumnCrossDelimiter);
                    else if (j < row.length - 1)
                        builder.append(borders.crossDelimiter);
                }

                if (frame)
                    builder.append(borders.rightIntersection);

                builder.append("\n");
            }
        }

        // outline
        if (frame) {
            builder.append("\n").append(borders.lowLeftCorner);
            for (int j = 0; j < firstRow.length; j++) {
                builder.append(String.valueOf(borders.horizontalOutline).repeat(firstRow[j].length()));

                if (j == 0)
                    builder.append(borders.firstColumnLowerIntersection);
                else if (j < firstRow.length - 1)
                    builder.append(borders.lowerIntersection);
            }
            builder.append(borders.lowRightCorner);
        }

        if (codeblock)
            builder.append("```");

        return builder.toString();

    }

    private void appendRow(StringBuilder builder, String[] row) {
        if (frame)
            builder.append(borders.verticalOutline);

        for (int i = 0; i < row.length; i++) {
            builder.append(row[i]);
            if (i == 0)
                builder.append(borders.firstColumnDelimiter);
            else if (i < row.length - 1)
                builder.append(borders.columnDelimiter);
        }

        if (frame)
            builder.append(borders.verticalOutline);

    }

    private void setAlignment(int adjustment, String oldValue, StringBuilder newValueBuilder) {
        // padding left
        newValueBuilder.append(" ".repeat(Math.max(0, padding)));

        switch (alignment) {
            case RIGHT -> newValueBuilder.append(" ".repeat(Math.max(0, adjustment))).append(oldValue); // then value
            case LEFT -> newValueBuilder.append(oldValue).append(" ".repeat(Math.max(0, adjustment)));
            case CENTER -> {
                int half = adjustment / 2;
                // append one half of black spaces
                newValueBuilder.append(" ".repeat(Math.max(0, half))).append(oldValue).append(" ".repeat(Math.max(0, half)));

                if (adjustment % 2 != 0) // if the number wasn't event, one blank space is still missing
                    newValueBuilder.append(" ");
            }
        }

        // padding right
        newValueBuilder.append(" ".repeat(Math.max(0, padding)));
    }

    public TableBuilder addHeaders(String... h) {
        headers = h;
        return this;
    }

    public TableBuilder addRowNames(String... rows) {
        rowNames = rows;
        return this;
    }

    public TableBuilder setValues(String[][] v) {
        values = v;
        return this;
    }

    public TableBuilder setBorders(Borders b) {
        borders = b;
        return this;
    }

    public TableBuilder setName(String tn) {
        tableName = tn;
        return this;
    }

    public TableBuilder setAlignment(Alignment al) {
        alignment = al;
        return this;
    }

    public TableBuilder setPadding(int p) {
        padding = p;
        return this;
    }

    public TableBuilder codeblock(boolean cb) {
        codeblock = cb;
        return this;
    }

    public TableBuilder frame(boolean f) {
        frame = f;
        return this;
    }

    public TableBuilder autoAdjust(boolean b) {
        autoAdjust = b;
        return this;
    }

    public enum Alignment {
        LEFT, RIGHT, CENTER
    }

    public static final class Borders {

        public static final Borders HEADER_ROW_FRAME = newHeaderRowNamesFrameBorders("─", "│", "┼",
            "├", "┤", "┬", "┴", "┌", "┐", "└",
            "┘", "═", "╪", "╞", "╡", "║",
            "╫", "╥", "╨", "╬", "─", "│");

        public static final Borders HEADER_FRAME = newHeaderFrameBorders("─", "│", "┼",
            "├", "┤", "┬", "┴", "┌", "┐", "└", "┘",
            "═", "╪", "╞", "╡", "─", "│");

        public static final Borders FRAME = newFrameBorders("─", "│", "┼", "├", "┤",
            "┬", "┴", "┌", "┐", "└", "┘", "─", "│");

        public static final Borders HEADER_ROW_PLAIN = newHeaderRowNamesPlainBorders("─", "│", "┼", "═",
            "╪", "║", "╫", "╬");

        public static final Borders HEADER_PLAIN = newHeaderPlainBorders("─", "│", "┼", "═", "╪");

        public static final Borders PLAIN = newPlainBorders("─", "│", "┼");

        public static final String UNKNOWN = "�";

        public final String rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection,
            upperIntersection, lowerIntersection, upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner,
            headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection,
            firstColumnDelimiter, firstColumnCrossDelimiter, firstColumnUpperIntersection,
            firstColumnLowerIntersection, headerColumnCrossDelimiter, horizontalOutline, verticalOutline;

        // framing + headers + rows
        private Borders(String rd, String cd, String crd, String li,
                        String ri, String ui, String le, String ul,
                        String ur, String ll, String lr, String hd,
                        String hcd, String hli, String hri,
                        String fcd, String fccd, String fcui,
                        String fcli, String hccd, String ho, String vo) {
            rowDelimiter = rd;
            columnDelimiter = cd;
            crossDelimiter = crd;
            leftIntersection = li;
            rightIntersection = ri;
            upperIntersection = ui;
            lowerIntersection = le;
            upLeftCorner = ul;
            upRightCorner = ur;
            lowLeftCorner = ll;
            lowRightCorner = lr;
            headerDelimiter = hd;
            headerCrossDelimiter = hcd;
            headerLeftIntersection = hli;
            headerRightIntersection = hri;
            firstColumnDelimiter = fcd;
            firstColumnCrossDelimiter = fccd;
            firstColumnUpperIntersection = fcui;
            firstColumnLowerIntersection = fcli;
            headerColumnCrossDelimiter = hccd;
            horizontalOutline = ho;
            verticalOutline = vo;
        }

        // framing + headers
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                        String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                        String lowRightCorner, String headerDelimiter, String headerCrossDelimiter, String headerLeftIntersection,
                        String headerRightIntersection, String horizontalOutline, String verticalOutline) {
            this(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection,
                headerRightIntersection, columnDelimiter, crossDelimiter, upperIntersection, lowerIntersection, headerCrossDelimiter,
                horizontalOutline, verticalOutline);
        }

        // framing
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                        String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                        String lowRightCorner, String horizontalOutline, String verticalOutline) {
            this(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, rowDelimiter, crossDelimiter, leftIntersection, rightIntersection,
                horizontalOutline, verticalOutline);
        }

        // plain + headers + rows
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter,
                        String firstColumnDelimiter, String firstColumnCrossDelimiter, String headerColumnCrossDelimiter) {
            this(rowDelimiter, columnDelimiter, crossDelimiter, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN,
                headerDelimiter, headerCrossDelimiter, UNKNOWN, UNKNOWN, firstColumnDelimiter, firstColumnCrossDelimiter,
                UNKNOWN, UNKNOWN, headerColumnCrossDelimiter, UNKNOWN, UNKNOWN);
        }

        // plain + headers
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter) {
            this(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter, columnDelimiter, crossDelimiter, headerCrossDelimiter);
        }

        // plain
        private Borders(String rowDelimiter, String columnDelimiter, String crossDelimiter) {
            this(rowDelimiter, columnDelimiter, crossDelimiter, rowDelimiter, crossDelimiter);
        }

        public static Borders newHeaderRowNamesFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection,
                                                            String rightIntersection, String upperIntersection, String lowerIntersection, String upLeftCorner,
                                                            String upRightCorner, String lowLeftCorner, String lowRightCorner, String headerDelimiter,
                                                            String headerCrossDelimiter, String headerLeftIntersection, String headerRightIntersection,
                                                            String firstColumnDelimiter, String firstColumnCrossDelimiter, String firstColumnUpperIntersection,
                                                            String firstColumnLowerIntersection, String headerColumnCrossDelimiter, String horizontalOutline, String verticalOutline) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection, upLeftCorner, upRightCorner,
                lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection, firstColumnDelimiter, firstColumnCrossDelimiter,
                firstColumnUpperIntersection, firstColumnLowerIntersection, headerColumnCrossDelimiter, horizontalOutline, verticalOutline);
        }

        public static Borders newHeaderFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                                                    String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                                                    String lowRightCorner, String headerDelimiter, String headerCrossDelimiter, String headerLeftIntersection,
                                                    String headerRightIntersection, String horizontalOutline, String verticalOutline) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection,
                upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, headerDelimiter, headerCrossDelimiter, headerLeftIntersection, headerRightIntersection,
                horizontalOutline, verticalOutline);
        }

        public static Borders newFrameBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String leftIntersection, String rightIntersection,
                                              String upperIntersection, String lowerIntersection, String upLeftCorner, String upRightCorner, String lowLeftCorner,
                                              String lowRightCorner, String horizontalOutline, String verticalOutline) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, leftIntersection, rightIntersection, upperIntersection, lowerIntersection, upLeftCorner, upRightCorner, lowLeftCorner, lowRightCorner, horizontalOutline, verticalOutline);
        }

        public static Borders newHeaderRowNamesPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter,
                                                            String firstColumnDelimiter, String firstColumnCrossDelimiter, String headerColumnCrossDelimiter) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter, firstColumnDelimiter, firstColumnCrossDelimiter, headerColumnCrossDelimiter);
        }

        public static Borders newHeaderPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter, String headerDelimiter, String headerCrossDelimiter) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter, headerDelimiter, headerCrossDelimiter);
        }

        public static Borders newPlainBorders(String rowDelimiter, String columnDelimiter, String crossDelimiter) {
            return new Borders(rowDelimiter, columnDelimiter, crossDelimiter);
        }
    }

}
