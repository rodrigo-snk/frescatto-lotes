package br.com.sankhya.frescatto.lotes.model;

import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.dao.EntityPrimaryKey;
import br.com.sankhya.jape.util.FinderWrapper;
import br.com.sankhya.jape.util.JapeSessionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.vo.EntityVO;
import br.com.sankhya.mgecomercial.model.facades.helpper.ItemNotaHelpper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.CentralItemNota;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.TipoOperacaoUtils;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ProdutoVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.modelcore.util.ParameterUtils;
import br.com.sankhya.ws.ServiceContext;
import com.sankhya.util.BigDecimalUtil;
import com.sankhya.util.StringUtils;
import com.sankhya.util.TimeUtils;

import javax.ejb.ObjectNotFoundException;
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
        itemNota.setProperty("VLRTOT", vlrUnit.multiply(itemNota.getQTDNEG()));
        dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, itemNota);
        criaEstoque(itemNota);

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
                criaEstoque(itemNota);

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
                criaEstoque(itemNota);


            }

            lote = proximoLote(lote);

        }
        //if (true) throw new MGEModelException("Produto: " + itemNota.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +itemNota.asBigDecimalOrZero("QTDNEG") + "\nTam. Max.: " + maxLote);


    }

    public static void desmembraLote(ItemNotaVO itemNota, CabecalhoNotaVO cabVO, DynamicVO varVO) throws Exception {
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
        itemNota.setProperty("VLRTOT", vlrUnit.multiply(itemNota.getQTDNEG()));
        dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, itemNota);
        // Altera a quantidade atendida na TGFVAR
        varVO.setProperty("QTDATENDIDA", maxLote);
        dwfFacade.saveEntity(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, (EntityVO) varVO);

        criaEstoque(itemNota);

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
                criaLigacaoVar(itemNota.asBigDecimal("NUNOTA"), itemNota.asBigDecimal("SEQUENCIA"), varVO.asBigDecimal("NUNOTAORIG"), varVO.asBigDecimal("SEQUENCIAORIG"), itemNota.getQTDNEG());
                prodVO.setProperty("AD_ULTCONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.PRODUTO, prodVO);
                criaEstoque(itemNota);

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
                criaLigacaoVar(itemNota.asBigDecimal("NUNOTA"), itemNota.asBigDecimal("SEQUENCIA"), varVO.asBigDecimal("NUNOTAORIG"), varVO.asBigDecimal("SEQUENCIAORIG"), itemNota.getQTDNEG());
                prodVO.setProperty("AD_ULTCONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.PRODUTO, prodVO);
                criaEstoque(itemNota);


            }

            lote = proximoLote(lote);

        }
        //if (true) throw new MGEModelException("Produto: " + itemNota.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +itemNota.asBigDecimalOrZero("QTDNEG") + "\nTam. Max.: " + maxLote);


    }

    private static void criaLigacaoVar(BigDecimal nuNota, BigDecimal sequencia, BigDecimal nuNotaOrig, BigDecimal sequenciaOrig, BigDecimal qtdAtendida) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
        DynamicVO newVarVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO);
        newVarVO.setProperty("NUNOTA", nuNota);
        newVarVO.setProperty("NUNOTAORIG", nuNotaOrig);
        newVarVO.setProperty("SEQUENCIAORIG", sequenciaOrig);
        newVarVO.setProperty("SEQUENCIA",sequencia);
        newVarVO.setProperty("QTDATENDIDA", qtdAtendida);
        dwfFacade.saveEntity(DynamicEntityNames.COMPRA_VENDA_VARIOS_PEDIDO, (EntityVO) newVarVO);

    }

    public static void geraLotes(ItemNotaVO itemVO, CabecalhoNotaVO cabVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemVO.asBigDecimalOrZero("CODPROD"), ProdutoVO.class);
        final boolean topTemLoteAutomatico = "S".equals(cabVO.asString("TipoOperacao.AD_LOTEAUT"));

        String ultimoLote = prodVO.asString("AD_ULTCONTROLE");
        final boolean ehMateriaPrima = "M".equals(prodVO.getUSOPROD());
        BigDecimal maxLote = prodVO.asBigDecimalOrZero("AD_MAXLOTE");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDNEG");
        String controle = itemVO.asString("CONTROLE");


        if (topTemLoteAutomatico && ehMateriaPrima && StringUtils.isEmpty(controle.trim())) {
            //if (true) throw new MGEModelException("Produto: " + itemVO.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +qtdNeg + "\nTam. Max.: " + maxLote);
            Collection<DynamicVO> itens = new ArrayList<>();
            if (qtdNeg.compareTo(maxLote) > 0) {
                desmembraLote(itemVO,cabVO);
            } else {
                lote = proximoLote(itemVO.getCODPROD());
                itemVO.setProperty("CONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, itemVO);
                criaEstoque(itemVO);
            }

            Lote.atualizaValoresItens(cabVO);

        /*
        // TESTE BASICO - SEMPRE ALTERA O LOTE DA MATERIA PRIMA
        if (ehMateriaPrima && controle.trim().isEmpty()) {
            itemVO.setProperty("CONTROLE", lote);
            dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
        }
        */

        }
    }

    public static void geraLotes(ItemNotaVO itemVO, CabecalhoNotaVO cabVO, DynamicVO varVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        ProdutoVO prodVO = (ProdutoVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.PRODUTO, itemVO.asBigDecimalOrZero("CODPROD"), ProdutoVO.class);
        final boolean topTemLoteAutomatico = "S".equals(cabVO.asString("TipoOperacao.AD_LOTEAUT"));

        String ultimoLote = prodVO.asString("AD_ULTCONTROLE");
        final boolean ehMateriaPrima = "M".equals(prodVO.getUSOPROD());
        BigDecimal maxLote = prodVO.asBigDecimalOrZero("AD_MAXLOTE");
        BigDecimal qtdNeg = itemVO.asBigDecimalOrZero("QTDNEG");
        String controle = itemVO.asString("CONTROLE");


        if (topTemLoteAutomatico && ehMateriaPrima && StringUtils.isEmpty(controle.trim())) {
            //if (true) throw new MGEModelException("Produto: " + itemVO.asBigDecimalOrZero("CODPROD") + "\nLote: " + lote + "\nQtd. Neg.: " +qtdNeg + "\nTam. Max.: " + maxLote);
            Collection<DynamicVO> itens = new ArrayList<>();
            if (qtdNeg.compareTo(maxLote) > 0) {
                desmembraLote(itemVO,cabVO,varVO);
            } else {
                lote = proximoLote(itemVO.getCODPROD());
                itemVO.setProperty("CONTROLE", lote);
                dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, itemVO);
                criaEstoque(itemVO);
            }

            Lote.atualizaValoresItens(cabVO);

        /*
        // TESTE BASICO - SEMPRE ALTERA O LOTE DA MATERIA PRIMA
        if (ehMateriaPrima && controle.trim().isEmpty()) {
            itemVO.setProperty("CONTROLE", lote);
            dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
        }
        */

        }
    }

    public static int prazoValidade (ItemNotaVO itemNotaVO) throws Exception {
        BigDecimal prazoValidadeProd = itemNotaVO.asBigDecimalOrZero("Produto.PRAZOVAL");
        if (!BigDecimalUtil.isNullOrZero(prazoValidadeProd)) return prazoValidadeProd.intValue();

        BigDecimal prazoValidadeGrupo = itemNotaVO.asBigDecimalOrZero("Produto.GrupoProduto.AD_PRAZOVAL");
        if (!BigDecimalUtil.isNullOrZero(prazoValidadeGrupo)) return prazoValidadeGrupo.intValue();

        BigDecimal prazoValidadeGeral = (BigDecimal) ParameterUtils.getParameter("PRAZOVAL");
        if (!BigDecimalUtil.isNullOrZero(prazoValidadeGeral)) return prazoValidadeGeral.intValue();

        return 1;

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

    public static void criaEstoque(ItemNotaVO itemVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO estVO = (DynamicVO) dwfFacade.getDefaultValueObjectInstance(DynamicEntityNames.ESTOQUE);

        try {
            estVO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ESTOQUE, new Object[] {itemVO.getCODEMP(), itemVO.getCODPROD(), itemVO.getCODLOCALORIG(), itemVO.getCONTROLE(), BigDecimal.ZERO, "P"});
            estVO.setProperty("DTFABRICACAO", itemVO.getProperty("CabecalhoNota.DTFATUR"));
            estVO.setProperty("DTVAL", TimeUtils.dataAddDay(estVO.asTimestamp("DTFABRICACAO"), Lote.prazoValidade(itemVO)));
            dwfFacade.saveEntity(DynamicEntityNames.ESTOQUE, (EntityVO) estVO);
        } catch (ObjectNotFoundException e) {
            estVO.setProperty("CODEMP", itemVO.getProperty("CODEMP"));
            estVO.setProperty("CODPROD", itemVO.getProperty("CODPROD"));
            estVO.setProperty("CONTROLE", itemVO.getProperty("CONTROLE"));
            estVO.setProperty("CODLOCAL", itemVO.getProperty("CODLOCALORIG"));
            estVO.setProperty("CODPARC", BigDecimal.ZERO);
            estVO.setProperty("TIPO", "P");
            estVO.setProperty("DTFABRICACAO", itemVO.getProperty("CabecalhoNota.DTFATUR"));
            estVO.setProperty("DTVAL", TimeUtils.dataAddDay(estVO.asTimestamp("DTFABRICACAO"), Lote.prazoValidade(itemVO)));
            dwfFacade.saveEntity(DynamicEntityNames.ESTOQUE, (EntityVO) estVO);
        }
    }

    public static void removeEstoque(ItemNotaVO itemVO) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO estVOO;

        try {
            estVOO = (DynamicVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ESTOQUE, new Object[] {itemVO.getCODEMP(), itemVO.getCODPROD(), itemVO.getCODLOCALORIG(), itemVO.getCONTROLE(), BigDecimal.ZERO, "P"});
            if (BigDecimalUtil.isNullOrZero(estVOO.asBigDecimalOrZero("ESTOQUE")) && BigDecimalUtil.isNullOrZero(estVOO.asBigDecimalOrZero("RESERVADO")))
                dwfFacade.removeEntity(DynamicEntityNames.ESTOQUE, (EntityPrimaryKey) estVOO.getPrimaryKey());
        } catch (ObjectNotFoundException ignored) {
        }
    }

    public static void atualizaValoresItens(DynamicVO cabVO) throws Exception {

        JapeSession.SessionHandle hnd = null;
        try {
            hnd = JapeSession.open();
            DynamicVO topVO  = TipoOperacaoUtils.getTopVO(cabVO.asBigDecimalOrZero("CODTIPOPER"));

            final boolean topPtaxDiaAnterior = topVO.containsProperty("AD_PTAXDIAANT") && "S".equals(StringUtils.getNullAsEmpty(topVO.asString("AD_PTAXDIAANT")));

            if (topPtaxDiaAnterior) {
                BigDecimal vlrMoeda = cabVO.asBigDecimalOrZero("VLRMOEDA");
                EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();
                //CabecalhoNotaVO cabecalhoNotaVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, cabVO.asBigDecimal("NUNOTA"), CabecalhoNotaVO.class);
                Collection<ItemNotaVO> itens = dwfFacade.findByDynamicFinderAsVO(new FinderWrapper(DynamicEntityNames.ITEM_NOTA, "this.NUNOTA = ?", cabVO.asBigDecimal("NUNOTA")), ItemNotaVO.class);

                //itens.forEach(vo -> vo.setProperty("VLRTOT", vo.getVLRUNIT().multiply(vo.getQTDNEG())));
                ServiceContext servico = ServiceContext.getCurrent();
                JapeSessionContext.putProperty("br.com.sankhya.com.CentralCompraVenda", Boolean.TRUE);
                JapeSessionContext.putProperty("ItemNota.incluindo.alterando.pela.central", Boolean.TRUE);

                for (DynamicVO itemVO: itens) {
                    atualizarItemNota(servico,cabVO, itemVO, itemVO.asBigDecimalOrZero("QTDNEG"), itemVO.asBigDecimal("VLRUNIT"));
                    dwfFacade.saveEntity(DynamicEntityNames.ITEM_NOTA, (EntityVO) itemVO);
                }
            }

        } catch (Exception e) {
            MGEModelException.throwMe(e);
        } finally {
            JapeSession.close(hnd);
        }

    }
    public static void atualizarItemNota(ServiceContext servico, DynamicVO cabVO, DynamicVO itemAtual, BigDecimal qtdNeg, BigDecimal vlrUnit) throws Exception {
        itemAtual.setProperty("VLRUNIT", vlrUnit);

        CentralItemNota itemNota = new CentralItemNota();
        itemNota.recalcularValores("VLRUNIT", vlrUnit.toString(), itemAtual, cabVO.asBigDecimalOrZero("NUNOTA"));

        List<DynamicVO> itensFatura = new ArrayList<>();
        itensFatura.add(itemAtual);

        CACHelper cacHelper = new CACHelper();
        cacHelper.incluirAlterarItem(cabVO.asBigDecimalOrZero("NUNOTA"), servico, null, false, itensFatura);
        //if(true) throw new MGEModelException("Vlr. Moeda:" + vlrCotacaoMoeda + "\nVlr Unit: " + itemAtual.asBigDecimalOrZero("VLRUNIT") + "\nVlr Total: " + itemAtual.asBigDecimalOrZero("VLRTOT") + "\nVlr Unit. Moeda: " + itemAtual.asBigDecimalOrZero("VLRUNITMOE"));

    }

}
