package br.com.sankhya.frescatto.lotes.model;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ProdutoVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;



public class Lote {

    public static String lote;


    public static void desmembraLote(ItemNotaVO itemNota, CabecalhoNotaVO cabVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        //Collection<DynamicVO> itens = new ArrayList<>();
        ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemNota.asBigDecimalOrZero("CODPROD"), ProdutoVO.class);
        //final boolean ehMateriaPrima = "M".equals(prodVO.getUSOPROD());
        BigDecimal qtdNeg = itemNota.asBigDecimalOrZero("QTDNEG");
        BigDecimal vlrUnit = itemNota.asBigDecimalOrZero("VLRUNIT");
        final BigDecimal maxLote = prodVO.asBigDecimalOrZero("AD_MAXLOTE");

        //Altera lote do primeiro item
        lote = proximoLote(prodVO.getCODPROD());
        itemNota.setProperty("QTDNEG",maxLote);
        itemNota.setProperty("CONTROLE",lote);
        dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemNota);


        if (!BigDecimalUtil.isNullOrZero(maxLote)) {

            BigDecimal[] divideAndRemainder = qtdNeg.subtract(maxLote).divideAndRemainder(maxLote);

            for (int i = 0; i < divideAndRemainder[0].intValue(); i++) {
                lote = proximoLote(lote);
                itemNota.setProperty("CONTROLE", lote);
                itemNota.setProperty("SEQUENCIA", null);
                itemNota.setProperty("QTDNEG", maxLote);
                itemNota.setProperty("VLRUNIT", vlrUnit);
                Collection<DynamicVO> itens = new ArrayList<>();
                itens.add(itemNota.wrapInterface(ItemNotaVO.class));
                ItemNotaHelpper.saveItensNota(itens, cabVO);
                prodVO.setProperty("AD_ULTCONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.PRODUTO, prodVO);
            }

            if (divideAndRemainder[1].compareTo(BigDecimal.ZERO) != 0){

                //lote = proximoLote(lote);
                lote = proximoLote(lote);

                itemNota.setProperty("CONTROLE", lote);
                itemNota.setProperty("SEQUENCIA", null);
                itemNota.setProperty("QTDNEG", divideAndRemainder[1]);
                itemNota.setProperty("VLRUNIT", vlrUnit);
                Collection<DynamicVO> itens = new ArrayList<>();
                itens.add(itemNota.wrapInterface(ItemNotaVO.class));
                ItemNotaHelpper.saveItensNota(itens, cabVO);
                prodVO.setProperty("AD_ULTCONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.PRODUTO, prodVO);
            }

            lote = proximoLote(lote);

        }
        //if (true) throw new MGEModelException("Produto: " + itemNota.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +itemNota.asBigDecimalOrZero("QTDNEG") + "\nTam. Max.: " + maxLote);


    }

    public static void geraLotes(ItemNotaVO itemVO, CabecalhoNotaVO cabVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemVO.asBigDecimalOrZero("CODPROD"), ProdutoVO.class);
        String ultimoLote = prodVO.asString("AD_ULTCONTROLE");
        final boolean ehMateriaPrima = "M".equals(prodVO.getUSOPROD());
        BigDecimal maxLote = prodVO.asBigDecimalOrZero("AD_MAXLOTE");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDNEG");
        String controle = itemVO.asString("CONTROLE");


        if (ehMateriaPrima && StringUtils.isEmpty(controle.trim())) {
            //if (true) throw new MGEModelException("Produto: " + itemVO.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +qtdNeg + "\nTam. Max.: " + maxLote);
            Collection<DynamicVO> itens = new ArrayList<>();
            if (qtdNeg.compareTo(maxLote) > 0) {
                desmembraLote(itemVO,cabVO);
            } else {
                lote = proximoLote(itemVO.getCODPROD());
                itemVO.setProperty("CONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
            }

        /*
        // TESTE BASICO - SEMPRE ALTERA O LOTE DA MATERIA PRIMA
        if (ehMateriaPrima && controle.trim().isEmpty()) {
            itemVO.setProperty("CONTROLE", lote);
            dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
        }
        */

        }
    }

    public static String proximoLote(BigDecimal codProd) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        HashSet<Integer> lotes = new HashSet<>();

        // Máscara do lote AAMMDD00000
        SimpleDateFormat simpleformat = new SimpleDateFormat("yyMMdd");
        String mascaraLote = simpleformat.format(new Date());

        /*FinderWrapper finder = new FinderWrapper(DynamicEntityNames.ESTOQUE, "this.CODPROD = ? and this.CONTROLE like '?%'", new Object[] {codProd, mascaraLote});
        Collection<DynamicVO> estoque = dwfFacade.findByDynamicFinderAsVO(finder);

        // Pega o sequencial de todos os lotes com a mascara 00000
        estoque.forEach(vo -> lotes.add(Integer.parseInt(vo.asString("CONTROLE").substring(6))));
        estoque.forEach(vo -> lotes.add(Integer.parseInt(vo.asString("CONTROLE").substring(6))));*/

        ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, codProd, ProdutoVO.class);
        String ultimoLote = StringUtils.getNullAsEmpty(prodVO.asString("AD_ULTCONTROLE"));

        final boolean loteDoDia = ultimoLote.length() >= 10 && ultimoLote.substring(0, 6).equals(mascaraLote);

        if (loteDoDia) {
            lotes.add(Integer.parseInt(ultimoLote.substring(6)));
        }


        return lotes.isEmpty() ? mascaraLote + String.format("%05d", 1) : mascaraLote + String.format("%05d", Collections.max(lotes) + 1);
    }

    public static String proximoLote(String lote) {
        String mascaraLote = lote.substring(0,6);
        return mascaraLote + String.format("%05d", Integer.parseInt(lote.substring(6)) + 1);
    }

}
