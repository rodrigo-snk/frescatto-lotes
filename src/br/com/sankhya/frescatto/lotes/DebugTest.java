package br.com.sankhya.frescatto.lotes;

import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import com.sankhya.util.TimeUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

public class DebugTest {

    public static void main(String[] args) {
        /*Timestamp hoje = TimeUtils.getNow();


        int ano = TimeUtils.getYear(hoje) % 100;
        int mes = TimeUtils.getMonth(hoje);

        System.out.println("Ano: " + ano);
        System.out.println("Mês: " + mes);

        Calendar c = Calendar.getInstance();

        int year = c.get(Calendar.YEAR) % 100;

        int month = c.get(Calendar.MONTH);

        System.out.println(year);
        System.out.println(month);*/

        // Máscara do lote AAMMDD00000
        SimpleDateFormat simpleformat = new SimpleDateFormat("yyMMdd");
        String lote = simpleformat.format(new Date());
        System.out.println("Lote Frescatto = " + lote);

        lote = lote + "00803";

        System.out.println(lote.substring(6));

        HashSet<Integer> lotes = new HashSet<>();

        int sequencia  = Integer.parseInt(lote.substring(6));

        lotes.add(sequencia);
        lotes.add(1);
        lotes.add(6);

        System.out.println(sequencia);

        System.out.println("Última sequência: " + Collections.max(lotes));

        System.out.println(String.format("%05d", Collections.max(lotes)+1));

        BigDecimal maxLote = BigDecimal.valueOf(900);
        BigDecimal qtdNeg = BigDecimal.valueOf(901);

        System.out.println("Comparação: " + (qtdNeg.compareTo(maxLote) > 0));

        int seqLote = Integer.parseInt(lote.substring(6)) + 1;

        System.out.println("Sequência: " +seqLote);

        System.out.println(lote.substring(0,6));

        String mascaraLote = lote.substring(0,6);

        System.out.println("Divisão inteiro: " + qtdNeg.divideToIntegralValue(maxLote));
        System.out.println(Arrays.toString(qtdNeg.divideAndRemainder(maxLote)));



        System.out.println(mascaraLote + String.format("%05d", Integer.parseInt(lote.substring(6)) + 1));


    }
}
